package de.jeff85.shared.fitnessreminder.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.ValidationException;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.jeff85.shared.fitnessreminder.ImageView;
import de.jeff85.shared.fitnessreminder.ReminderView;
import de.jeff85.shared.fitnessreminder.controller.model.Reminder;

public class ReminderController {

	private static final String GROUP_NAME = "group1";
	private static final String TRIGGER_NAME = "trigger1";
	private static final String DEFAULT_NAME = "defaultReminder";
	private static final String FILE_NAME = "FitnessReminder.json";
	private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);
	
	private final ReminderView view;

	public ReminderController(ReminderView view) {
		this.view = view;
	}

	public void saveReminder(String imagePath, String text, int startHours, int endHours, int repeatMinutes) throws IOException, SchedulerException, ValidationException {
		String cronExpression = "0 0/" + repeatMinutes + " " + startHours + "-" + endHours + " * * ?";
		String actualText = text == null ? "" : text;
		if (startHours <= 0 || endHours <= 0 || repeatMinutes <= 0 || imagePath.isEmpty()) {
			throw new ValidationException("Invalid parameters");
		}
		Reminder reminder = createReminder(imagePath, cronExpression, actualText, startHours, endHours, repeatMinutes);
		try {
			logger.info("Rescheduling reminder ...");
			TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, GROUP_NAME);
			Scheduler sched = initScheduler();
			sched.unscheduleJob(triggerKey);
			sched.deleteJob(new JobKey(TRIGGER_NAME));
			scheduleJob(reminder, sched);
		} catch (SchedulerException e) {
			e.printStackTrace();
			throw e;
		}

		Gson gson = new Gson();
		String json = gson.toJson(reminder);
		Files.write(getConfigFile(), json.getBytes(StandardCharsets.UTF_8));
	}

	public void previewReminder(String imagePath, String text) {
		URL imageUrl = new FilepathToUrlConverter().getUrl(imagePath);
		new ImageView(imageUrl, text);
	}

	public void loadReminder() throws IOException, SchedulerException {
		Gson gson = new Gson();
		String json = new String(Files.readAllBytes(getConfigFile()), StandardCharsets.UTF_8);
		Reminder reminder = gson.fromJson(json, Reminder.class);
		try {
			Scheduler sched = initScheduler();
			scheduleJob(reminder, sched);
		} catch (SchedulerException e) {
			e.printStackTrace();
			throw e;
		}
		view.onReminderLoaded(reminder);
	}

	private Reminder createReminder(String imagePath, String cronExpression, String text, int startHours, int endHours,
			int repeatMinutes) {
		return new Reminder(DEFAULT_NAME, imagePath, cronExpression, text, startHours, endHours, repeatMinutes);
	}
	
	private void scheduleJob(Reminder reminder, Scheduler sched) throws SchedulerException {
		JobDetail job = JobBuilder.newJob(ReminderScheduledJob.class).withIdentity(reminder.getName(), GROUP_NAME)
				.usingJobData(ReminderScheduledJob.KEY_IMAGE_PATH, reminder.getImagePath())
				.usingJobData(ReminderScheduledJob.KEY_TEXT, reminder.getText()).build();

		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME, GROUP_NAME)
				.withSchedule(CronScheduleBuilder.cronSchedule(reminder.getCronExpression())).build();
		sched.scheduleJob(job, trigger);
	}

	private Scheduler initScheduler() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		Scheduler sched = schedFact.getScheduler();
		if (!sched.isStarted())
			sched.start();
		return sched;
	}
	
	public Path getConfigFile() {
		return Paths.get(System.getProperty("user.home") + "/" + FILE_NAME);
	}

}