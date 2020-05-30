package de.devjeff.fitnessreminder.controller;

import java.net.URL;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.devjeff.fitnessreminder.ImageView;

public class ReminderScheduledJob implements Job {
	public static final String KEY_IMAGE_PATH = "KEY_IMAGE_PATH";
	public static final String KEY_TEXT = "KEY_TEXT";
	
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    	String imagePath = jobDataMap.get(KEY_IMAGE_PATH).toString();
    	URL imageUrl = new FilepathToUrlConverter().getUrl(imagePath);
		ImageView.show(imageUrl, jobDataMap.get(KEY_TEXT).toString());
    }

	
}