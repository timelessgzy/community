package cn.tjgzy.community.service;

import cn.tjgzy.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.Set;

/**
 * @author GongZheyi
 * @create 2021-10-03-15:54
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param userId 点赞的用户
     * @param entityType    点赞的实体对象类型
     * @param entityId  点赞的实体对象ID
     * @param entityUserId 实体作者的用户ID
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        BoundSetOperations operations = redisTemplate.boundSetOps(entityLikeKey);
//        if (operations.isMember(userId)) {
//            operations.remove(userId);
//        } else {
//            operations.add(userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);
                // 开启事务
                operations.multi();
                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey,userId);
                    // 减少被赞数量
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    // 维护被赞数量
                    operations.opsForValue().increment(userLikeKey);
                }
                operations.exec();
                return null;
            }
        });
    }

    /**
     * 查询实体点赞的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        BoundSetOperations operations = redisTemplate.boundSetOps(entityLikeKey);
        Long size = operations.size();
        return size;
    }

    /**
     * 查询某人对某实体是否点赞
     * @param userId
     * @param entityType
     * @param entityId
     * @return 1为点赞，0为未点赞
     */
    public int findEntityLikeStatus (int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        BoundSetOperations operations = redisTemplate.boundSetOps(entityLikeKey);
        return operations.isMember(userId) ? 1 : 0;
    }

    /**
     * 查询某个用户获得的赞
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();



    }

}
