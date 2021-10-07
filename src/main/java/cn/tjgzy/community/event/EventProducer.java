package cn.tjgzy.community.event;

import cn.tjgzy.community.entity.Event;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author GongZheyi
 * @create 2021-10-05-11:59
 */
@Component
public class EventProducer {
    @Autowired
    KafkaTemplate kafkaTemplate;

    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }


}
