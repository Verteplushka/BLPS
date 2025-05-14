package com.example.BLPS.config;

import com.example.BLPS.Quartz.UpdateRatingsJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfig {
    private final ApplicationContext applicationContext;

    // Внедряем ApplicationContext
    public QuartzConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public JobDetail updateRatingsJobDetail() {
        return JobBuilder.newJob(UpdateRatingsJob.class)
                .withIdentity("updateRatingsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger updateRatingsTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(updateRatingsJobDetail())
                .withIdentity("updateRatingsTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10) // Каждые 10 секунд
                        .repeatForever())
                .build();
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.scheduleJob(updateRatingsJobDetail(), updateRatingsTrigger());
        scheduler.start();
        return scheduler;
    }
    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        SpringBeanJobFactory factory = new SpringBeanJobFactory();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

}
