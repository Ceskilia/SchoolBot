# SchoolBot
This project incorporates the content of timetables from a school website into a discord bot.

## 📖 About
The main function of this bot is to display timetable information from a school's website in discord periodically or by user's request.

In addition, several utilities are provided:
- periodic posting in a designated channel
- commands
- user verification
- blacklisting
- rate limiting
- timetable caching

This project is based on the library **[JDA](https://github.com/discord-jda/JDA)** and self-made projects **CUtils** (for utilities) and **ConfigUtil** (for easy config management). The later two are not published yet.

## ℹ️ Overview

### Broadcast channel
For every guild, a separate channel can be selected, in which timetables are posted periodically at given times for each day.

> The interval between two such posting times must be at least 30 minutes.

### Commands
The following commands implemented:

- `/changelog`: see development updates
- `/owner`: see information on config and user / guild records; update user permissions
- `/ping`: see the ping between the bot and discord
- `/setup`: set up the broadcast channel (admin only)
- `/timetable`: request a timetable with a specific date; potentially update it
- `/verify`: user verification with credentials

### User verification & blacklisting
In order to use and see timetables, a user must verify himself with the right credentials. Additionally, users can be blacklisted from requesting timetable information.

### Rate limiting
To limit the number of requests to the school's website, only a limited number of new requests are allowed in a specific time interval.

> The default ratelimit is **max. 5 requests per minute**.

### Caching
Timetables are cached to reduce to amount of requests made to the website. 

Per default, the cached information of a timetable is returned whenever present.
When requesting an update, the timetable needs to be updatable, i.e. after the last web request of this very timetable, at least `UPDATE_INTERVAL` milliseconds have to pass by.

```java
/**
 * The time interval in milliseconds for a timetable to be updatable after initial creation.
 */
long UPDATE_INTERVAL = 600_000; // 10min
```

## ⚙️ Setup & Configuration

### Config
The config files are generated automatically when starting the program for the first time.
- In **settings.json**, the token for the bot and the corresponding timetable url should be provided.
- In **channels.json**, the per-guild channels are given in which timetable updates are posted periodically. This file is mainly used as record storage.
- In **verification.json**, the credentials for the website authentication should be provided. This config also includes verified and blacklisted users, as record storage.

> Most values in the config can be adjusted at runtime and the change will get applied immediately.

### Logging
For every program execution, the console output is put into a separate log file. The corresponding log level can be adjusted in the [logback.xml](./src/main/resources/logback.xml):

```xml
<root level="DEBUG">   <!-- adjust log level here -->
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="FILE" />
</root>
```

## 🚀 Installation & Execution
To build the project with all dependencies, use:

```
./gradlew shadowJar
```

To run the bot, either execute the jar directly or use the following console-command:

```
./gradlew run
```

