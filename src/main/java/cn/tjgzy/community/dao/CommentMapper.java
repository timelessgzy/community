package cn.tjgzy.community.dao;

import cn.tjgzy.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author GongZheyi
 * @create 2021-10-01-10:57
 */
@Mapper
public interface CommentMapper {
    /**
     * 根据实体类型与实体ID 查询评论
     * @param entityType 实体类型，1代表帖子，2代表评论，3代表用户
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);
}
