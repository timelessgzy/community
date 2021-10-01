package cn.tjgzy.community.interceptor;

import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author GongZheyi
 * @create 2021-09-30-17:36
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = hostHolder.getUser();
        if (user == null) {
            response.sendRedirect("/login");
        }
        return true;
    }
}
