package cn.tjgzy.community.actuator;

import cn.tjgzy.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author GongZheyi
 * @create 2021-10-09-8:53
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation  // 该端点由get请求访问
    public String checkConnection() {
        try(Connection connection = dataSource.getConnection();) {
            return CommunityUtil.getJSONString(0,"获取连接成功！");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("获取连接失败");
            return CommunityUtil.getJSONString(1,"获取连接失败");
        }
    }
}
