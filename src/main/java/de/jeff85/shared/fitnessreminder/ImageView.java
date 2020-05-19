package de.jeff85.shared.fitnessreminder;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageView extends JFrame {
	private static final int INITIAL_FONT_SIZE = 80;
	private static final int IMG_WIDTH = 1000;
	private static final int TEXT_PADDING = 20;
	
	public ImageView(URL imageUrl, String message) {
		super("Fitness Reminder");
		JLabel label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		try {
			BufferedImage originalImage = ImageIO.read(imageUrl);
			Image updatedImage = drawTextOnImage(originalImage, message);
			label.setIcon(new ImageIcon(updatedImage));
		} catch (IOException e) {
			System.err.println("Failed to load image: " + imageUrl);
			e.printStackTrace();
		}

		getContentPane().add(label, BorderLayout.CENTER);
		setVisible(true);
		pack();
		setLocationRelativeTo(null); // location is relative to the main screen in case there are multiple screens
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		toFront();
		requestFocus();
	}
	
	private Image drawTextOnImage(BufferedImage old, String message) {
		String[] lines = message.split("\n");
		if (lines.length > 2) {
			lines = new String[] { lines[0], lines[1] };
		}
		int w = IMG_WIDTH;
		double scaleFactor = old.getWidth() * 1.0 / IMG_WIDTH;
		boolean isScalingDown = old.getWidth() > IMG_WIDTH;
		int h =  (int) (isScalingDown ? old.getHeight() / scaleFactor : old.getHeight() * scaleFactor);

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(old, 0, 0, w, h, this);

		Font font = new Font(Font.SANS_SERIF, Font.BOLD, INITIAL_FONT_SIZE);
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		String longestLine = lines.length == 1 ? lines[0]
				: (lines[0].length() > lines[1].length() ? lines[0] : lines[1]);
		while (getTextWidth(fm, longestLine) > img.getWidth()) {
			font = new Font(Font.SANS_SERIF, Font.BOLD, font.getSize() - 2);
			g2d.setFont(font);
			fm = g2d.getFontMetrics();
		}

		g2d.setStroke(new BasicStroke(3));

		int lineCount = 1;
		for (String line : lines) {
			int textWidth = getTextWidth(fm, line);
			int x = (img.getWidth() - textWidth + TEXT_PADDING) / 2;
			int y = lineCount == 1 ? fm.getHeight() - 10 : (img.getHeight() - 20);
			lineCount++;
			if (line.isEmpty()) 
				continue;
			
			TextLayout tl = new TextLayout(line, font, g2d.getFontRenderContext());
			Shape shape = tl.getOutline(null);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			AffineTransform affineTransform = new AffineTransform();
			affineTransform.setToTranslation(x, y);
			g2d.setTransform(affineTransform);

			g2d.setPaint(Color.WHITE);
			g2d.fill(shape);
			g2d.setPaint(Color.BLACK);
			g2d.draw(shape);
		}
		g2d.dispose();
		return img;
	}

	private int getTextWidth(FontMetrics fm, String line) {
		return line.chars().map((charInt) -> fm.getWidths()[charInt]).sum() + TEXT_PADDING;
	}
}
