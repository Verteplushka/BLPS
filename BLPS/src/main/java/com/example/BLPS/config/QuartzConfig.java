package com.example.BLPS.config;

import com.example.BLPS.Quartz.AutowiringSpringBeanJobFactory;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.quartz.spi.JobFactory;

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
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.scheduleJob(updateRatingsJobDetail(), updateRatingsTrigger());
        scheduler.start();
        return scheduler;
    }
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        return factory;
    }

}
