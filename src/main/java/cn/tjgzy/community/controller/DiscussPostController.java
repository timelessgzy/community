package cn.tjgzy.community.controller;

import cn.tjgzy.community.entity.Comment;
import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.entity.Page;
import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.CommentService;
import cn.tjgzy.community.service.DiscussPostService;
import cn.tjgzy.community.service.LikeService;
import cn.tjgzy.community.service.UserService;
import cn.tjgzy.community.util.CommunityConstant;
import cn.tjgzy.community.util.CommunityUtil;
import cn.tjgzy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author GongZheyi
 * @create 2021-10-01-9:13
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403,"您还未登录");
        }
        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setUserId(user.getId());
        discussPostService.addDiscussPost(post);
        // TODO：如果插入失败，后续进行统一处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable int discussPostId, Model model, Page page) {
        // 查询帖子内容
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        // 查询帖子的作者
        User author = userService.findUserById(post.getUserId());
        model.addAttribute("user",author);

        // 帖子的点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);

        // 当前用户是否点赞
        User user = hostHolder.getUser();
        int status = (user == null ? 0 :
                likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_POST, discussPostId));
        model.addAttribute("likeStatus",status);


        // TODO:帖子的回复
        // 评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        // 这里查的是post表里的冗余字段
        page.setRows(post.getCommentCount());

        // 评论：给帖子的评论
        // 回复：给评论的评论

        // 评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(),
                page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment: commentList) {
                // 单个评论的vo
                Map<String,Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment",comment);
                // 作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                // 帖子的点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);

                // 当前用户是否点赞
                status = user == null ? 0 :
                        likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",status);

                // 回复列表
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);
                // 回复VO 列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply: replyList) {
                        // 单个回复的Vo
                        Map<String,Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply",reply);
                        // 作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        // 帖子的点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);

                        // 当前用户是否点赞
                        status = user == null ? 0 :
                                likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",status);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }



}
