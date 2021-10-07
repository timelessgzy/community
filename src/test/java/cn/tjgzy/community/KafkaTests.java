package cn.tjgzy.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author GongZheyi
 * @create 2021-10-04-19:37
 */
@SpringBootTest
public class KafkaTests {
    @Autowired
    KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
        kafkaProducer.sendMessage("comment","haha");
        kafkaProducer.sendMessage("comment","你好呀！");
        kafkaProducer.sendMessage("comment","gzygzy");

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

@Component
class KafkaProducer {
    @Autowired
    KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic,content);
    }
}

@Component
class KafkaConsumer {
    @KafkaListener(topics = {"comment"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }

}
