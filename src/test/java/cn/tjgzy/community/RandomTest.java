package cn.tjgzy.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author GongZheyi
 * @create 2021-10-05-23:48
 */
@SpringBootTest
public class RandomTest {
    @Test
    public void test() {
        Random random = new Random();
        int i = random.nextInt(1);
        Map<Integer,String> map = new HashMap<>();
        map.put(0,"肉蟹煲");
        map.put(1,"湊湊");

        System.out.println(map.get(i));
    }

}
