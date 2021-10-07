package cn.tjgzy.community.interceptor;

import cn.tjgzy.community.entity.LoginTicket;
import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.UserService;
import cn.tjgzy.community.util.CookieUtil;
import cn.tjgzy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author GongZheyi
 * @create 2021-09-30-14:45
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            System.out.println(loginTicket);
            // 判断是否失效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                System.out.println("未失效！");
                User user = userService.findUserById(loginTicket.getUserId());
                System.out.println("prehandle:" + user);
                // 暂存user
                hostHolder.setUser(user);

                // 构建用户认证结果，并存入SecurityContext，以便于security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(), userService.getAuthorities(user.getId()));
                // 存到securityContext中
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
