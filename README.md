# Android Restaurant Hours

- DISCLAIMER: Reused some tutorial code from https://github.com/google-developer-training/basic-android-kotlin-compose-training-mars-photos

## Commands

- Command to run Android Emulator:
  - `C:\Users\<username>\AppData\Local\Android\Sdk\emulator\emulator -avd <emulated phone name> -feature -Vulkan`
    - Example: `C:\Users\solde\AppData\Local\Android\Sdk\emulator\emulator -avd Pixel_3a_API_34_extension_level_7_x86_64 -feature -Vulkan`
- Open up Android Studio as well to actually compile the app onto the emulator (Shift + F10)
- Formatting:
  - `java -jar ktfmt-0.47-jar-with-dependencies.jar --kotlinlang-style <full path to project>`
    - Example: `java -jar ktfmt-0.47-jar-with-dependencies.jar --kotlinlang-style "C:\Users\solde\AndroidStudioProjects\android_business_hours"`
    - Download kfmt v0.47 from https://github.com/facebook/ktfmt/releases, put it in the root directory of this repo (this file is in `.gitignore` due to large file size)

## Assumptions

**General Assumptions**

- all days from api endpoint would be in "first 3 letters of day of week name" format
- all events happen same each week
- local timezone on user matches server
- am pm final format (confirmed by figma)
- when in doubt, check how google maps mobile app does the UI

## Test Cases
Edit `val timestamp` in `app\src\main\java\com\example\businesshours\ui\screens\HomeScreen.kt`
See https://www.unixtimestamp.com/index.php to convert Unix for debug
- 1716973724000 (wednesday 2:08am) -> wednesday 7am - 1pm
- 1716970124000 (wednesday 1:08am) -> tuesday 3pm - (wednesday) 2am
- 1716998924000 (wednesday 9:08am) -> wednesday 7am - 1pm
- 1717013324000 (wednesday 1:08pm) -> wednesday 3pm - 10pm
- 1717049324000 (wednesday 11:08pm) -> thursday 24h
- 1717023600000 (wednesday 4:00pm) -> wednesday 3pm - 10pm
- TODO: 24h back to back (feed in fake data local var / json)
- 1717196400000 (Friday 4:00pm) -> tuesday 7am - 1pm

## Screenshot
![](https://github-production-user-asset-6210df.s3.amazonaws.com/82061589/335072139-cd64b0ad-78f6-4cfb-8c6b-0647b2d10dad.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20240530%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240530T065247Z&X-Amz-Expires=300&X-Amz-Signature=db5a51f17b881abfe4a31c52c81a20a9666bcfbde0fd98c3d5784ac106bc42ab&X-Amz-SignedHeaders=host&actor_id=82061589&key_id=0&repo_id=804817281)