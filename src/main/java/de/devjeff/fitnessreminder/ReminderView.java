package de.devjeff.fitnessreminder;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.xml.bind.ValidationException;

import org.quartz.SchedulerException;

import de.devjeff.fitnessreminder.controller.ReminderController;
import de.devjeff.fitnessreminder.controller.model.Reminder;

@SuppressWarnings("serial")
public class ReminderView extends JFrame {
	private static final String GITHUB_LINK = "https://github.com/devjeff/fitness-reminder";
	private TrayIcon trayIcon;
	private SystemTray tray;
	private ReminderController controller;

	private JTextField startTimeField;
	private JTextField endTimeField;
	private JTextField repeatField;
	private JTextField imagePathField;
	private JTextField textField;
	private JTextField textField2;

	public ReminderView() {
		super("Fitness Reminder");
		controller = new ReminderController(this);
		Image image = ViewUtil.getResourceImage("alarm.png");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Unable to set LookAndFeel");
		}
		if (SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();

			PopupMenu popup = createTrayMenu();
			trayIcon = new TrayIcon(image, "Fitness Reminder", popup);
			trayIcon.setImageAutoSize(true);
		} else {
			System.out.println("system tray not supported");
		}

		getContentPane().add(createContent(), BorderLayout.CENTER);
		addTrayIconHandling();

		setIconImage(image);
		setVisible(true);
		pack();
		setLocationRelativeTo(null); // location is relative to the main screen in case there are multiple screens
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		if (Files.exists(controller.getConfigFile())) {
			try {
				controller.loadReminder();
			} catch (IOException | SchedulerException e) {
				JOptionPane.showMessageDialog(ReminderView.this,
						"Error while loading reminder config: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private Component createContent() {
		JPanel mainPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 4, 4, 4);

		// First line ============================================================== //
		JLabel startTimeLabel = new JLabel("Starting (hours) from:");
		c.gridx = 0;
		c.gridy = 0;
		mainPanel.add(startTimeLabel, c);

		startTimeField = new JTextField("8");
		startTimeField.setPreferredSize(new Dimension(60, 20));
		startTimeField.setMinimumSize(startTimeField.getPreferredSize());
		c.gridx = 1;
		c.gridy = 0;
		mainPanel.add(startTimeField, c);

		JLabel endTimeLabel = new JLabel("to:");
		c.gridx = 2;
		c.gridy = 0;
		mainPanel.add(endTimeLabel, c);

		endTimeField = new JTextField("19");
		endTimeField.setPreferredSize(new Dimension(60, 20));
		endTimeField.setMinimumSize(endTimeField.getPreferredSize());
		c.gridx = 3;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(endTimeField, c);
		c.anchor = GridBagConstraints.LINE_END;

		// Second line ============================================================ //
		JLabel repeatLabel = new JLabel("Repeat every:");
		c.gridx = 0;
		c.gridy = 1;
		mainPanel.add(repeatLabel, c);
		c.anchor = GridBagConstraints.CENTER;

		repeatField = new JTextField("30");
		repeatField.setPreferredSize(new Dimension(60, 20));
		repeatField.setMinimumSize(repeatField.getPreferredSize());
		c.gridx = 1;
		c.gridy = 1;
		mainPanel.add(repeatField, c);

		JLabel timeUnitLabel = new JLabel("minutes");
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(timeUnitLabel, c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = 1;

		// Third line ============================================================== //
		JLabel imagePathLabel = new JLabel("Image path:");
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_END;
		mainPanel.add(imagePathLabel, c);
		c.anchor = GridBagConstraints.CENTER;

		imagePathField = new JTextField();
		imagePathField.setEnabled(false);
		imagePathField.setPreferredSize(new Dimension(120, 20));
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 3;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(imagePathField, c);
		c.gridwidth = 1;
		c.weightx = 0;

		JButton chooseImageButton = new JButton(new ImageIcon(ViewUtil.getResourceImage("open_folder.png")));
		c.gridx = 1;
		c.gridy = 3;
		mainPanel.add(chooseImageButton, c);
		chooseImageButton.addActionListener((event) -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			int result = fileChooser.showOpenDialog(ReminderView.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				imagePathField.setText(selectedFile.getAbsolutePath());
			}
		});
		
		JButton chooseResourceImageButton = new JButton(new ImageIcon(ViewUtil.getResourceImage("open_pics.png")));
		c.gridx = 2;
		c.gridy = 3;
		mainPanel.add(chooseResourceImageButton, c);
		chooseResourceImageButton.addActionListener((event) -> {
			String selectedPath = PredefinedBackgroundsView.show(ReminderView.this);
			if (selectedPath != null) {
				imagePathField.setText(selectedPath);
			}
		});

		// Fourth line ============================================================== //
		JLabel textLabel = new JLabel("Displayed text line 1:");
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.LINE_END;
		mainPanel.add(textLabel, c);
		c.anchor = GridBagConstraints.CENTER;

		textField = new JTextField("Come on");
		textField.setPreferredSize(new Dimension(60, 20));
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 4;
		c.weightx = 0.5;
		mainPanel.add(textField, c);
		c.gridwidth = 1;
		c.weightx = 0;

		// Fifth line ============================================================== //
		textLabel = new JLabel("Displayed text line 2:");
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.LINE_END;
		mainPanel.add(textLabel, c);
		c.anchor = GridBagConstraints.CENTER;

		textField2 = new JTextField("You know you need it!");
		textField2.setPreferredSize(new Dimension(60, 20));
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 4;
		c.weightx = 0.5;
		mainPanel.add(textField2, c);
		c.gridwidth = 1;
		c.weightx = 0;

		// Buttons ============================================================== //
		JButton saveButton = new JButton("Save");
		c.gridx = 5;
		c.gridy = 0;
		mainPanel.add(saveButton, c);
		saveButton.addActionListener((event) -> {
			try {
				controller.saveReminder(imagePathField.getText(), getDisplayText(),
						Integer.parseInt(startTimeField.getText()), Integer.parseInt(endTimeField.getText()),
						Integer.parseInt(repeatField.getText()));
			} catch (NumberFormatException | IOException | SchedulerException | ValidationException e) {
				JOptionPane.showMessageDialog(ReminderView.this, "Error while saving: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		JButton previewButton = new JButton("Preview");
		c.gridx = 5;
		c.gridy = 1;
		mainPanel.add(previewButton, c);
		previewButton.addActionListener((event) -> {
			try {
				controller.previewReminder(imagePathField.getText(), getDisplayText());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(ReminderView.this, "Error while loading image: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		Image buttonIcon = ViewUtil.getResourceImage("info.png");
		JButton infoButton = new JButton(new ImageIcon(buttonIcon));
		infoButton.setPreferredSize(new Dimension(64, 64));
		infoButton.setBorderPainted(false);
		infoButton.setFocusPainted(false);
		infoButton.setContentAreaFilled(false);
		c.gridx = 5;
		c.gridy = 4;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.BASELINE;
		mainPanel.add(infoButton, c);
		infoButton.addActionListener((event) -> {
			try {
				controller.openUrlInBrowser(GITHUB_LINK);
			} catch (UnsupportedOperationException e) {
				JTextField infoText = new JTextField("Please visit: " + GITHUB_LINK);
				infoText.setEditable(false);
				JOptionPane.showMessageDialog(ReminderView.this, infoText, "Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		return mainPanel;
	}

	private String getDisplayText() {
		return textField.getText() + "\n" + textField2.getText();
	}

	private void addTrayIconHandling() {
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() % 2 == 0 && !e.isConsumed()) {
					e.consume();
					setVisible(true);
					setExtendedState(JFrame.NORMAL);
				}
			}
		});

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if (tray.getTrayIcons().length == 0) {
						tray.add(trayIcon);
						setVisible(false);
					}
				} catch (AWTException ex) {
					System.out.println("unable to add to tray");
				}
			}

		});

		addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				if (e.getNewState() == ICONIFIED) {
					try {
						setVisible(false);
						if (tray.getTrayIcons().length == 0)
							tray.add(trayIcon);
					} catch (AWTException ex) {
						System.out.println("unable to add to tray");
					}
				}
				if (e.getNewState() == 7 && tray.getTrayIcons().length == 0) {
					try {
						setVisible(false);
						if (tray.getTrayIcons().length == 0)
							tray.add(trayIcon);
					} catch (AWTException ex) {
						System.out.println("unable to add to system tray");
					}
				}
//				if (e.getNewState() == MAXIMIZED_BOTH) {
//					tray.remove(trayIcon);
//					setVisible(true);
//				}
//				if (e.getNewState() == NORMAL) {
//					tray.remove(trayIcon);
//					setVisible(true);
//				}
			}
		});
	}

	private PopupMenu createTrayMenu() {
		ActionListener exitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		PopupMenu popup = new PopupMenu();
		MenuItem defaultItem = new MenuItem("Exit");
		defaultItem.addActionListener(exitListener);
		popup.add(defaultItem);
		defaultItem = new MenuItem("Open");
		defaultItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
				setExtendedState(JFrame.NORMAL);
			}
		});
		popup.add(defaultItem);
		return popup;
	}

	public void onReminderLoaded(Reminder reminder) {
		if (reminder != null) {
			startTimeField.setText("" + reminder.getStartHours());
			endTimeField.setText("" + reminder.getEndHours());
			repeatField.setText("" + reminder.getRepeatMinutes());
			imagePathField.setText(reminder.getImagePath());

			String[] lines = reminder.getText().split("\\n");
			textField.setText(lines[0]);
			if (lines.length > 1) {
				textField2.setText(lines[1]);
			}
		}
	}
}