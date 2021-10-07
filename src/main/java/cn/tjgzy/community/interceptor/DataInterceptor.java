package cn.tjgzy.community.interceptor;

import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.DataService;
import cn.tjgzy.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author GongZheyi
 * @create 2021-10-07-19:50
 */
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        dataService.recordUV(request.getRemoteHost());

        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
