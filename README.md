# [Bonfire](https://play.google.com/store/apps/details?id=sh.sit.bonfire)

[![CI Status](https://github.com/timas130/bonfire/workflows/Continuous%20Integration/badge.svg)](https://github.com/timas130/bonfire/actions)
[![Discord](https://img.shields.io/discord/798617754118127636.svg?label=&logo=discord&logoColor=ffffff&color=5865f2)](https://discord.gg/QGcJrHknAp)
[![EN Translation Status](https://tlp.bonfire.moe/widgets/bonfire/en/svg-badge.svg)](https://tlp.bonfire.moe/projects/bonfire/-/en)
[![RU Translation Status](https://tlp.bonfire.moe/widgets/bonfire/ru/svg-badge.svg)](https://tlp.bonfire.moe/projects/bonfire/-/ru)
[![UK Translation Status](https://tlp.bonfire.moe/widgets/bonfire/uk/svg-badge.svg)](https://tlp.bonfire.moe/projects/bonfire/-/uk)

A social network for Android with many communities and a unique moderation system: its users can
earn moderator privileges by completing achievements and gaining karma (likes). It has an extensive
post editor that supports Markdown, images, GIFs, polls, etc. There are chats for each community,
private chats between users (DMs) and private conferences.

## Features

* Anyone can create and curate their own community: there are wiki pages, quests, relay races and
  rubrics to help other communities engage with it.
* Users have full control over media: anyone can become a moderator and everyone can vote for
  adding a new rule or feature to any community.
* Community participation rewards: from special badges to moderation abilities depending on your
  level and karma. You can earn exclusive privileges by creating engaging spaces!

## Building the app

You can build and run the app using Android Studio. [Java 17](https://jdk.java.net/archive) is
recommended.

## Testing locally (building the server)

1. Ensure you have the following:
   * [Android Studio](https://developer.android.com/studio) or
     [Java 17](https://jdk.java.net/archive) with
     [Command line tools](https://developer.android.com/studio#command-line-tools-only) to build
     the app
   * [Docker](https://docker.com/products/docker-desktop)
   * [PostgreSQL database](https://postgresql.org/downloads) (the database should be named
     "postgres")
   * [MinIO server and client](https://min.io/open-source/download)

2. Create a Firebase project:
   1. Open https://console.firebase.google.com
   2. Create a new project
   3. Click on `Add app` and select Android
   4. Enter package name (e.g. "sh.sit.devfire")
   5. Optionally enter app nickname and click on `Register app`
   6. Go back to the console
   7. Click on blue `Add app` and select Android
   8. Enter the same package name and type ".debug" at the end (e.g. "sh.sit.devfire.debug")
   9. Optionally enter app nickname and click on `Register app`
   10. You can now close the console

3. Download `google-services.json` (Project settings -> General) and Admin SDK JSON (Project
   settings -> Service accounts -> Generate new private key) from
   [Firebase Console](https://console.firebase.google.com)

4. Download and unpack this archive into another directory:
   https://drive.google.com/file/d/1V2cVKJ8JpRt7h_l64umXf75GxDjEkNgX

5. Open `local.properties`, `config.toml` and `secrets/Secrets.json`:
   1. Replace "\<internal host\>" with the IP address of your computer in your local network
      (run `ip addr` or `ifconfig`) in `local.properties` and `Secrets.json`
   2. Set database creds in `Secrets.json` (lines 9-16) and `config.toml` (line 1)
   3. Copy data from Admin SDK JSON to `Secrets.json` (lines 45-55) and `config.toml` (lines 49-59)
   4. Set S3 address and keys in `Secrets.json` (lines 58-60)
   5. Set the package ID you've previously entered (without ".debug") and optionally the
      app's name in `local.properties` (lines 7-8)

6. Clone this repo and copy `local.properties`, `config.toml` and `secrets/Secrets.json` to its
   root

7. Replace `Campfire/google-services.json` with your `google-services.json`

8. Import the database dump and upload static resources to MinIO (change S3 address and keys to
   yours in `alias set local`):

   On Linux:
   ```
   sudo -u postgres psql \
       -c "\i dump.sql" \
       -c "alter database postgres set search_path = campfire_db,campfire_db_media,public"

   mcli alias set local http://localhost:9000 admin secretsecret
   mcli mb -p local/bonfire
   mcli anonymous set public local/bonfire
   ls -1 static | xargs -I {} mcli put static/{} local/bonfire/static/{}
   ```

   On Windows (PowerShell):
   ```
   psql.exe `
       -c "\i dump.sql" `
       -c "alter database postgres set search_path = campfire_db,campfire_db_media,public"

   mc.exe alias set local http://localhost:9000 admin secretsecret
   mc.exe mb -p local/bonfire
   mc.exe anonymous set public local/bonfire
   gci -path static -file | foreach {
       mc.exe put $_.fullname local/bonfire/static/$_.name
   }
   ```

9. Build and install the app

10. Build and run the server:

    On Linux:
    ```
    docker build -t bonfire .
    docker run --network=host -v./secrets:/app/secrets -v./config.toml:/app/config.toml bonfire:latest
    ```

    On Windows:
    ```
    docker.exe build -t bonfire .
    docker.exe run --network=host -v./secrets:/app/secrets -v./config.toml:/app/config.toml bonfire:latest
    ```

The server must not be exposed to public! If everything went smoothly, you can now open your custom
Bonfire and log in as "default@user.com" with the password "00000000" (8 zeros). If you want to add
another account, run this to verify it:

On Linux:
```
sudo -u postgres psql -c "update users set email_verified = now(), email_verification_sent = null where email_verified is null"
```

On Windows:
```
psql.exe -c "update users set email_verified = now(), email_verification_sent = null where email_verified is null"
```

## License

This project contains components licensed differently. See [LICENSE](LICENSE) for full details.
