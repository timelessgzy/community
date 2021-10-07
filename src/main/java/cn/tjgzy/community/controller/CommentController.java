package cn.tjgzy.community.controller;

import cn.tjgzy.community.entity.Comment;
import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.entity.Event;
import cn.tjgzy.community.event.EventProducer;
import cn.tjgzy.community.service.CommentService;
import cn.tjgzy.community.service.DiscussPostService;
import cn.tjgzy.community.util.CommunityConstant;
import cn.tjgzy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author GongZheyi
 * @create 2021-10-01-14:54
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @PostMapping("/add/{discussPostId}")
    public String addComment(Comment comment, @PathVariable int discussPostId) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        // 设置作者
        // 到底是给帖子评论，还是给帖子的回复评论？
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        // 发布事件
        System.out.println("controller开始发布事件了！");
        eventProducer.fireEvent(event);


        // 发布第二个事件，同步到es服务器
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }
}
