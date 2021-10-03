package cn.tjgzy.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * @author GongZheyi
 * @create 2021-10-03-15:36
 */
@SpringBootTest
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(redisKey,1);
        valueOperations.increment(redisKey);
        System.out.println(valueOperations.get(redisKey));

    }


}
