package de.devjeff.fitnessreminder;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;

public class ViewUtil {

	public static Image getResourceImage(String imgName) {
		URL imgUrl = ReminderView.class.getClassLoader().getResource(imgName);
		Image image = Toolkit.getDefaultToolkit().getImage(imgUrl);
		return image;
	}
	
	public static BufferedImage getScaledImage(BufferedImage old, int newWidth, ImageObserver observer) {
		double scaleFactor = old.getWidth() * 1.0 / newWidth;
		boolean isScalingDown = old.getWidth() > newWidth;
		int h =  (int) (isScalingDown ? old.getHeight() / scaleFactor : old.getHeight() * scaleFactor);
		BufferedImage img = new BufferedImage(newWidth, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(old, 0, 0, img.getWidth(), img.getHeight(), observer);
		return img;
	}
}
