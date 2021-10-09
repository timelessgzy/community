package cn.tjgzy.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(CommunityApplicationTests.class);

    @Test
    void contextLoads() {
    }

    @Test
    public void test1() {
        logger.info("test");
        System.out.println("test");
        logger.debug("test");
    }

}
