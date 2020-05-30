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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.devjeff.fitnessreminder.controller.FilepathToUrlConverter;

public class PredefinedBackgroundsView extends JDialog {

	private String selectedPath;

	public static String show(JFrame parent) {
		PredefinedBackgroundsView view = new PredefinedBackgroundsView(parent);
		view.setVisible(true);
		view.dispose();
		return FilepathToUrlConverter.PREDEFINED_URI_SCHEME + new File(view.selectedPath).getName();
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
			Path myPath = getPredefinedPath();
			Files.list(myPath).forEach((path) -> grid.add(createImageCard(path)));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return grid;
	}

	private Path getPredefinedPath() throws URISyntaxException, IOException {
		URI predefinedPicPath = PredefinedBackgroundsView.class.getClassLoader().getResource("predefined").toURI();
		Path myPath;
		if (predefinedPicPath.getScheme().equals("jar")) {
			try (FileSystem fileSystem = FileSystems.newFileSystem(predefinedPicPath,
					Collections.<String, Object>emptyMap())) {
				myPath = fileSystem.getPath("predefined");
			}
		} else {
			myPath = Paths.get(predefinedPicPath);
		}
		return myPath;
	}

	private Component createImageCard(Path imgPath) {
		JLabel label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		try {
			BufferedImage originalImage = ImageIO.read(imgPath.toFile());
			BufferedImage updatedImage = ViewUtil.getScaledImage(originalImage, 400, this);
			label.setIcon(new ImageIcon(updatedImage));
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					selectedPath = imgPath.toString();
					setVisible(false);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return label;
	}
}
