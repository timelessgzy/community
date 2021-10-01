package cn.tjgzy.community.dao;

import cn.tjgzy.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author GongZheyi
 * @create 2021-09-29-19:54
 */
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

}
