package cn.tjgzy.community.quartz;

import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.service.DiscussPostService;
import cn.tjgzy.community.service.ElasticsearchService;
import cn.tjgzy.community.service.LikeService;
import cn.tjgzy.community.util.CommunityConstant;
import cn.tjgzy.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author GongZheyi
 * @create 2021-10-08-18:43
 */
@Component
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 牛客纪元
     */
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-10 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败",e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            logger.info("没有需要刷新的帖子，任务取消！");
            return;
        }

        logger.info("任务开始，正在刷新帖子分数，帖子个数为：" + operations.size());

        while (operations.size() > 0) {
            int postId = (int) operations.pop();
            // 刷新帖子分数
            refresh(postId);
        }
        logger.info("任务结束，正在刷新帖子分数，帖子个数为：" + operations.size());
    }

    public void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在，id=" + postId);
            return;
        }
        // 是否加精
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 1000 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w,1))
                    + (post.getCreateTime().getTime() - epoch.getTime() / (1000 * 3600 *24));

        // 更新帖子的分数
        discussPostService.updateScore(postId,score);
        post.setScore(score);

        // 同步搜索数据
        elasticsearchService.saveDiscussPost(post);

    }
}
