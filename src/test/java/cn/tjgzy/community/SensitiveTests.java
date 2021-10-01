package cn.tjgzy.community;

import cn.tjgzy.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author GongZheyi
 * @create 2021-09-30-21:01
 */
@SpringBootTest
public class SensitiveTests {
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "赌赌赌赌赌博";
        String filter = sensitiveFilter.filter(text);
        System.out.println(filter);
    }

}
