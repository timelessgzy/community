package cn.tjgzy.community.util;

import cn.tjgzy.community.entity.User;
import org.springframework.stereotype.Component;

/** 持有用户信息，代替session对象
 * @author GongZheyi
 * @create 2021-09-30-14:53
 */
@Component
public class HostHolder {


    private ThreadLocal<User> users = new ThreadLocal<>();


    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}
