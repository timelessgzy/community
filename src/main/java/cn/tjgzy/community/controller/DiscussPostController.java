package cn.tjgzy.community.controller;

import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.DiscussPostService;
import cn.tjgzy.community.service.UserService;
import cn.tjgzy.community.util.CommunityUtil;
import cn.tjgzy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author GongZheyi
 * @create 2021-10-01-9:13
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

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
    public String getDiscussPost(@PathVariable int discussPostId, Model model) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        // 查询帖子的作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        // TODO:帖子的回复
        return "/site/discuss-detail";
    }

}
