package cn.tjgzy.community.service;

import cn.tjgzy.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author GongZheyi
 * @create 2021-10-07-19:32
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定ip记入uv
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    // 统计指定日期范围内的uv
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 得到该日期范围内的key集合
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        // 合并key集合
        String redisKey = RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }


    /**
     * 将指定用户记入DAU
     * @param userId
     */
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的DAU
    // 比如9月3日的活跃用户为1号和3号，9月4日的活跃用户为2号和3号，那么这两天的活跃用户一共有3个
    // 因此将每天的活跃用户bitmap进行or运算后进行统计即可
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 得到该日期范围内的key集合
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start),df.format(end));
                connection.bitOp(
                        RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),
                        keyList.toArray(new byte[0][0])
                );
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }




}
