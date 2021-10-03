package cn.tjgzy.community.util;

/**
 * @author GongZheyi
 * @create 2021-10-03-15:50
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";


    /**
     * 某个实体的赞
     * like:entity:entityType:entityId ---> 是一个set{userId}
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的点赞数
     * like:user:userId ---> int
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_ENTITY_LIKE + SPLIT + userId;
    }

}
