package cn.tjgzy.community;

import cn.tjgzy.community.dao.DiscussPostMapper;
import cn.tjgzy.community.dao.LoginTicketMapper;
import cn.tjgzy.community.dao.MessageMapper;
import cn.tjgzy.community.dao.UserMapper;
import cn.tjgzy.community.entity.DiscussPost;
import cn.tjgzy.community.entity.LoginTicket;
import cn.tjgzy.community.entity.Message;
import cn.tjgzy.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

/**
 * @author GongZheyi
 * @create 2021-09-29-19:56
 */
@SpringBootTest
public class MapperTests {
    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    MessageMapper messageMapper;

    @Test
    public void testSelect() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost discussPost: discussPosts) {
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        int i = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(i);
    }

    @Test
    public void testMessageMapper() {
        Message message = new Message();
        message.setContent("111");
        message.setCreateTime(new Date());
        message.setConversationId("test");
        message.setToId(10);
        message.setFromId(1);
        message.setStatus(1);
        messageMapper.insertMessage(message);
        int i = messageMapper.selectLetterCount("111_112");
        System.out.println(i);
    }
}
