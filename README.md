# fitness-reminder

Simple java application to set up visual reminders to do fitness exercises

When working at the computer we often forget to do fitness or simple stretching exercises. This application shows a popup containing a user-selected image with a text in regular intervals, so that we don't forget to make a small break and do some exercises.

<img style=" margin: 10px auto 20px; display: block;" src="src/test/resources/screenshot.png" alt="App screenshot">

**Latest version**
- [Fitness Reminder 1.0.1](https://github.com/devjeff/fitness-reminder/releases/download/v1.0.1/fitness-reminder-1.0.1.jar)
<br/><br/>

**Why not just using one of the plenty mobile apps?**

Push notifications can get pretty annoying. Besides, I've noticed that I tend to ignore the phone vibrations. Thus, I came to the conclusion that a visual reminder that is shown directly on the screen where you're working at, is much more reliable and can't be ignored that easily.


**How to use**

It's very simple. First, you enter the desired reminder interval in minutes. Now you can select the image that will displayed after each interval timeout. There are 2 options:
1. Pick an image from your hard drive (via the folder button)
2. Pick a predifined image that's contained in the JAR file (via the picture button)

Then, you can write the desired text that will be displayed with the image. The first text line will be displayed at the top and the second at the bottom of the image. Choose a good and motivating message here. Finally, don't forget to save your configuration, so that the information doesn't get lost when you close the app and open it again.

By the way, the app is put into system tray when you press the close or minimize buttons. You can right-click on the system tray icon and press "Exit" to actually terminate the app. The app configuration is stored as a JSON file in the user's home directory.


**System requirements**

Java runtime environment with at least version 1.8


**Special thanks to**

- [pixabay.com](https://pixabay.com/) for providing lots of beautiful free images
- [iconfinder.com](https://www.iconfinder.com/) for providing lots of cool and free icons

