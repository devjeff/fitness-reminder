package de.devjeff.fitnessreminder.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FilepathToUrlConverter {

	private static final String PREDEFINED_IMG_FOLDER = "predefined";
	public static final String PREDEFINED_URI_SCHEME = "predefined://";

	public URL getUrl(String filePath) {
		URL fileUrl = null;
		try {
			File file = new File(filePath);
			if (filePath != null && filePath.startsWith(PREDEFINED_URI_SCHEME)) {
				return ReminderScheduledJob.class.getClassLoader()
						.getResource(PREDEFINED_IMG_FOLDER + "/" + filePath.replace(PREDEFINED_URI_SCHEME, ""));
			}

			if (file.exists()) {
				fileUrl = file.toURI().toURL();
			} else {
				ReminderScheduledJob.class.getClassLoader().getResource("not_found.png");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return fileUrl;
	}

}
