package de.skrelpoid.fivemstats.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLongTermDataJob implements Job {
	
	private static final Logger logger = LoggerFactory.getLogger(CreateLongTermDataJob.class);

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.info("Creating long term data");
	}

}
