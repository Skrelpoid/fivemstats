package de.skrelpoid.fivemstats.job;
import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import de.skrelpoid.fivemstats.data.service.AutoWiringSpringBeanJobFactory;
import jakarta.annotation.PostConstruct;

@Configuration
public class SpringQrtzScheduler {
	
	private static final int ONE_MINUTE = 60;

    private static final Logger logger = LoggerFactory.getLogger(SpringQrtzScheduler.class);

    @PostConstruct
    public void init() {
        logger.info("Hello world from Spring...");
    }

    @Bean
    SpringBeanJobFactory springBeanJobFactory(final ApplicationContext applicationContext) {
        final AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        logger.debug("Configuring Job factory");

        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    SchedulerFactoryBean scheduler(final Trigger[] triggers, final JobDetail[] jobs, final DataSource quartzDataSource, final SpringBeanJobFactory jobFactory) {

        final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        logger.debug("Setting the Scheduler up");
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setJobDetails(jobs);
        schedulerFactory.setTriggers(triggers);


        return schedulerFactory;
    }

    @Bean("queryJob")
    JobDetailFactoryBean queryJob() {

        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(QueryPlayerDataJob.class);
        jobDetailFactory.setName("query");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean("queryTrigger")
    SimpleTriggerFactoryBean queryTrigger(final JobDetail queryJob) {

        final SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(queryJob);

        logger.info("Configuring trigger to fire every {} seconds", ONE_MINUTE);

        trigger.setRepeatInterval(ONE_MINUTE * 1000L);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName("query_Trigger");
        return trigger;
    }

    @Bean("longTermJob")
    JobDetailFactoryBean longTermJob() {

        final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(CreateLongTermDataJob.class);
        jobDetailFactory.setName("longTermData");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean("longTermTrigger")
    CronTriggerFactoryBean longTermTrigger(final JobDetail longTermJob) {

        final CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(longTermJob);

        trigger.setCronExpression("0 3 2 1 * ? *");
        trigger.setName("longTerm_Trigger");
        return trigger;
    }

}