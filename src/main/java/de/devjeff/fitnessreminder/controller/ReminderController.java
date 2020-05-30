package de.devjeff.fitnessreminder.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.xml.bind.ValidationException;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.devjeff.fitnessreminder.ImageView;
import de.devjeff.fitnessreminder.ReminderView;
import de.devjeff.fitnessreminder.controller.model.Reminder;

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

	public void saveReminder(String imagePath, String text, int startHours, int endHours, int repeatMinutes)
			throws IOException, SchedulerException, ValidationException {
		String cronExpression = "0 0/" + repeatMinutes + " " + startHours + "-" + endHours + " * * ?";
		String actualText = text == null ? "" : text;
		if (repeatMinutes <= 0 || imagePath.isEmpty()) {
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
		ImageView.show(imageUrl, text);
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

	public Path getConfigFile() {
		return Paths.get(System.getProperty("user.home") + "/" + FILE_NAME);
	}

	public void openUrlInBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private Reminder createReminder(String imagePath, String cronExpression, String text, int startHours, int endHours,
			int repeatMinutes) {
		return new Reminder(DEFAULT_NAME, imagePath, cronExpression, text, startHours, endHours, repeatMinutes);
	}

	private void scheduleJob(Reminder reminder, Scheduler sched) throws SchedulerException {
		JobDetail job = JobBuilder.newJob(ReminderScheduledJob.class).withIdentity(reminder.getName(), GROUP_NAME)
				.usingJobData(ReminderScheduledJob.KEY_IMAGE_PATH, reminder.getImagePath())
				.usingJobData(ReminderScheduledJob.KEY_TEXT, reminder.getText()).build();

		Trigger trigger;
		if (reminder.getStartHours() == 0 && reminder.getEndHours() == 0) {
			long startTimeMs = System.currentTimeMillis() + (reminder.getRepeatMinutes() * 60 * 1000);
			trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME, GROUP_NAME).startAt(new Date(startTimeMs))
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInMinutes(reminder.getRepeatMinutes()).repeatForever())
					.build();
		} else {
			trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME, GROUP_NAME)
					.withSchedule(CronScheduleBuilder.cronSchedule(reminder.getCronExpression())).build();
		}

		sched.scheduleJob(job, trigger);
	}

	private Scheduler initScheduler() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		Scheduler sched = schedFact.getScheduler();
		if (!sched.isStarted())
			sched.start();
		return sched;
	}

}
