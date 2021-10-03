package cn.tjgzy.community.controller;

import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.entity.Page;
import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.DiscussPostService;
import cn.tjgzy.community.service.LikeService;
import cn.tjgzy.community.service.UserService;
import cn.tjgzy.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GongZheyi
 * @create 2021-09-29-20:31
 */
@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String ,Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post: list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                // 查询点赞数
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }
}
