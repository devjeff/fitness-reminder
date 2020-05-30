package de.devjeff.fitnessreminder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.devjeff.fitnessreminder.controller.FilepathToUrlConverter;

public class PredefinedBackgroundsView extends JDialog {

	private URI selectedPath;
	private static String pattern = "(/|" + Pattern.quote(System.getProperty("file.separator")) + ")";
	private static PredefinedBackgroundsView instance;
	
	public static String show(JFrame parent) {
		instance = instance == null ? new PredefinedBackgroundsView(parent) : instance;
		instance.setVisible(true);
//		instance.dispose();

		if (instance.selectedPath != null) {
			String[] splittedFileName = instance.selectedPath.toString().split(pattern);
			return FilepathToUrlConverter.PREDEFINED_URI_SCHEME + splittedFileName[splittedFileName.length - 1];
		}
		return null;
	}

	public PredefinedBackgroundsView(JFrame parent) {
		super(parent, "Choose background", Dialog.ModalityType.APPLICATION_MODAL);
		getContentPane().add(buildCardView(), BorderLayout.CENTER);
		setIconImage(ViewUtil.getResourceImage("alarm.png"));
//		setVisible(true);
		pack();
		setLocationRelativeTo(null); // location is relative to the main screen in case there are multiple screens
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	}

	private Component buildCardView() {
		GridLayout layout = new GridLayout(3, 4);
		layout.setHgap(8);
		layout.setVgap(8);
		final JPanel grid = new JPanel();
		grid.setLayout(layout);

		try {
			URI predefinedPicPath = PredefinedBackgroundsView.class.getClassLoader().getResource("predefined").toURI();
			Path myPath;
			if (predefinedPicPath.getScheme().equals("jar")) {
				// TODO pretty hacky solution. Is it really that complicated?
				CodeSource src = PredefinedBackgroundsView.class.getProtectionDomain().getCodeSource();
				List<String> list = new ArrayList<String>();
				if (src != null) {
					URL jar = src.getLocation();
					try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
						ZipEntry ze = null;
						while ((ze = zip.getNextEntry()) != null) {
							String entryName = ze.getName();
							if (entryName.startsWith("predefined") && entryName.endsWith(".jpg")) {
								list.add(entryName);
							}
						}
					}
					for (String entry : list) {
						grid.add(createImageCard(
								PredefinedBackgroundsView.class.getClassLoader().getResource(entry).toURI()));
					}
				}
			} else {
				myPath = Paths.get(predefinedPicPath);
				Files.list(myPath).forEach((path) -> grid.add(createImageCard(path.toUri())));
			}
		} catch (IOException |

				URISyntaxException e) {
			e.printStackTrace();
		}
		return grid;
	}

	private Component createImageCard(URI imgPath) {
		JLabel label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		try {
			BufferedImage originalImage = ImageIO.read(imgPath.toURL());
			BufferedImage updatedImage = ViewUtil.getScaledImage(originalImage, 400, this);
			label.setIcon(new ImageIcon(updatedImage));
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					selectedPath = imgPath;
					setVisible(false);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return label;
	}

}
