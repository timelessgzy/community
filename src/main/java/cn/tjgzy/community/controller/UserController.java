package cn.tjgzy.community.controller;

import cn.tjgzy.community.annotation.LoginRequired;
import cn.tjgzy.community.entity.Comment;
import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.entity.Page;
import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.*;
import cn.tjgzy.community.util.CommunityConstant;
import cn.tjgzy.community.util.CommunityUtil;
import cn.tjgzy.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GongZheyi
 * @create 2021-09-30-15:18
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));

        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;

        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败！" + e.getMessage());
        }

        // 更新当前用户头像的路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(HttpServletResponse response, @PathVariable String fileName) {
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        try(
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
                ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    @LoginRequired
    public String updatePassword(Model model, String oldPassword, String newPassword) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map.containsKey("passwordMsg")) {
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/setting";
        }
        return "redirect:/index";
    }


    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable int userId, Model model) {
         User user = userService.findUserById(userId);
         if (user == null) {
             throw new RuntimeException("该用户不存在！");
         }
         // 用户
         model.addAttribute("user",user);
         // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);



        return "/site/profile";

    }

    @GetMapping("/post/{userId}")
    public String getUserPosts(@PathVariable int userId, Model model, Page page) {
        int rows = discussPostService.findDiscussPostRows(userId);
        page.setRows(rows);
        page.setPath("/user/post/" + userId);

        model.addAttribute("rows",rows);
        System.out.println("该用户了一共发表了" + rows);

        List<DiscussPost> posts = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 1);

        List<Map<String,Object>> list = new ArrayList<>();

        for (DiscussPost post: posts) {
            Map<String,Object> map = new HashMap<>();
            map.put("post",post);
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
            map.put("likeCount", likeCount);
            System.out.println(post);
            System.out.println(likeCount);
            list.add(map);
        }

        model.addAttribute("list",list);

        model.addAttribute("userId",userId);
        return "/site/my-post";
    }


    @GetMapping("/comment/{userId}")
    public String getUserComments(@PathVariable int userId, Model model, Page page) {


        // 查总数
        int rows = commentService.findCountByUserId(userId);
        model.addAttribute("rows",rows);

        page.setRows(rows);
        page.setPath("/user/comment/" + userId);
        page.setLimit(5);

        // 查回复
        List<Comment> comments = commentService.findCommentsByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();

        for (Comment comment: comments) {
            Map<String,Object> map = new HashMap<>();
            // 查帖子
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            map.put("comment",comment);
            map.put("post",post);
            list.add(map);
        }

        model.addAttribute("list", list);
        model.addAttribute("userId",userId);
        return "/site/my-reply";
    }



}
