# YTAP

Telegram bot that downloads audio from YouTube and sends it to the user as an audio file. [You can try it now!](http://t.me/ytap_bot)

![Hello, YTAP!](https://i.imgur.com/tgFdsjU.png)

## Dependencies and requirements
* Unix-like OS — Ubuntu, Debian, CentOS or macOS — that's all should work;
* JDK 11 is minimal *(I guess... Actually I'm using AdoptOpenJDK 14)*;
* Maven;
* [Telgrambots](https://github.com/rubenlagus/TelegramBots) library;
* [YouTube-DL](https://github.com/ytdl-org/youtube-dl) installed;
* [FFmpeg](https://github.com/FFmpeg/FFmpeg) installed;

## Getting started
0\. Install JDK. I use [AdoptOpenJDK14](https://adoptopenjdk.net) with OpenJ9 VM.

1\. Clone repository. If you are using an IntelliJ IDEA, you can grab all project directly from GitHub by link.

2\. Add **telegrambots** dependency in your *pom.xml*:
```xml
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots</artifactId>
    <version>4.9</version>
</dependency>
```

I also recommend using compiler source and target for Maven:
```xml
<properties>
    <maven.compiler.source>14</maven.compiler.source>
    <maven.compiler.target>14</maven.compiler.target>
</properties>
```

3\. Create files **name.txt** and **token.txt** in working directory. Add a bot name and token, according to the file names. If you don't have it ask [*@BotFather*](https://core.telegram.org/bots#6-botfather) at Telegram.

4\. Create an empty **./audio** directory.

The end result should be something like this:
![Directory test things isn't required](https://i.imgur.com/XghsLqB.png)

5\. Install youtube-dl. You can do that from system package manager, but packages may be quite old (except *brew* on Mac and *pacman* in Arch). [Here](https://github.com/ytdl-org/youtube-dl#installation) is a better way to install the latest version.
*Make sure, that you can call youtube-dl from /usr/local/bin/youtube-dl. If not, [create a symlink](https://askubuntu.com/questions/56339/how-to-create-a-soft-or-symbolic-link).*

6\. Install FFmpeg. You can use your system package manager here.

7\. Build the project with any available method. I use an artifact (JAR) [build with IntelliJ IDEA](https://stackoverflow.com/questions/1082580/how-to-build-jars-from-intellij-properly).

8\. **[Optional]** If you deploy your bot to VPS, you probably want to run it in the background. You can use [nohup](https://linux.die.net/man/1/nohup) for that, but better way would be a **Systemd** service.

1. Create in *\*.service* file in */etc/systemd/system/*. For example, it would be *ytap.service*.
2. Your service may look like that:
    ```
    [Unit]
    Description=Manage Java ytap service
    [Service]
    WorkingDirectory=/var/www/telegrambots/ytap/
    ExecStart=/bin/java -jar YTAP.jar
    User=ytap
    Type=simple
    Restart=on-failure
    RestartSec=10
    [Install]
    WantedBy=multi-user.target
    ```
3. Save ytap.service file, restart systemd, enable service and start it:
    ```bash
    $ sudo systemctl daemon-reload
    $ sudo systemctl enable ytap.service
    $ sudo systemctl start ytap.service
    ```
Now your bot runs in background, automatically restarts on failure and after server reboot.

9\. **[Optional]** Add a cron job to regularly update youtube-dl. This will prevent errors related to YouTube changes. The cron job looks like this:

```
0 3 * * * /usr/local/bin/youtube-dl -U >> /var/www/telegrambots/ytap/bot.log
```

If there is a problem with the update, the information will be logged.
