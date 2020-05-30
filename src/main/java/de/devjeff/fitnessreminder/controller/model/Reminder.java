package de.devjeff.fitnessreminder.controller.model;


public class Reminder {

	private final String name;
	private final String imagePath;
	private final String text;
	// start, end hours and cron expression are currently not used
	private final int startHours;
	private final int endHours;
	private final String cronExpression;
	private final int repeatMinutes;
	
	public Reminder(String name, String imagePath, String scheduledTime, String text, int startHours, int endHours,
			int repeatMinutes) {
		this.name = name;
		this.imagePath = imagePath;
		this.text = text;
		this.cronExpression = scheduledTime;
		this.startHours = startHours;
		this.endHours = endHours;
		this.repeatMinutes = repeatMinutes;
	}

	public String getName() {
		return name;
	}

	public String getImagePath() {
		return imagePath;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public int getStartHours() {
		return startHours;
	}

	public int getEndHours() {
		return endHours;
	}

	public int getRepeatMinutes() {
		return repeatMinutes;
	}

	public String getText() {
		return text;
	}
	
	
	
}
