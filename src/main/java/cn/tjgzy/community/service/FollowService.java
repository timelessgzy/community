package cn.tjgzy.community.service;

import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.util.CommunityConstant;
import cn.tjgzy.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author GongZheyi
 * @create 2021-10-04-9:35
 */
@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;


    /**
     * 用户关注某实体
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                BoundZSetOperations followeeZset = operations.boundZSetOps(followeeKey);
                followeeZset.add(entityId,System.currentTimeMillis());
                BoundZSetOperations followerZset = operations.boundZSetOps(followerKey);
                followerZset.add(userId,System.currentTimeMillis());
                return operations.exec();
            }
        });

    }

    public void unfollow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                BoundZSetOperations followeeZset = operations.boundZSetOps(followeeKey);
                followeeZset.remove(entityId,System.currentTimeMillis());
                BoundZSetOperations followerZset = operations.boundZSetOps(followerKey);
                followerZset.remove(userId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    /**
     * 查询关注的实体的数量
     * @param userId
     * @param entityType
     * @return
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        BoundZSetOperations operations = redisTemplate.boundZSetOps(followeeKey);
        Long size = operations.zCard();
        return size;
    }

    /**
     * 查询某实体被关注的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否关注某实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        BoundZSetOperations operations = redisTemplate.boundZSetOps(followeeKey);
        Double score = operations.score(entityId);
        return score != null;
    }

    /**
     * 查询某用户关注的人
     * @return map中保存user和score
     */
    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        BoundZSetOperations operations = redisTemplate.boundZSetOps(followeeKey);
        Set<Integer> targetIds = operations.reverseRange(offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (int targetId: targetIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = operations.score(targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    /**
     * 查询某人关注的粉丝
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String,Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        BoundZSetOperations operations = redisTemplate.boundZSetOps(followerKey);
        Set<Integer> targetIds = operations.reverseRange(offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (int targetId: targetIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = operations.score(targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


}
