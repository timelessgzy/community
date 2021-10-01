package cn.tjgzy.community;

import cn.tjgzy.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author GongZheyi
 * @create 2021-09-30-8:42
 */
@SpringBootTest
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Test
    public void sendTest() {
        mailClient.sendMail("854606081@qq.com","hello","welcome!");
    }
}
