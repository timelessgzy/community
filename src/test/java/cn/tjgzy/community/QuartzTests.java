package cn.tjgzy.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author GongZheyi
 * @create 2021-10-08-18:16
 */
@SpringBootTest
public class QuartzTests {
    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDelete() {
        try {
            boolean b = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }


}
