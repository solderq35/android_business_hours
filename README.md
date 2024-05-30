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

- all days from api endpoint would be in "first 3 letters of day of week name" format?
- safe to reorder by day?
- all events happen same each week
- local timezone on user matches server
- am pm final format (confirmed by figma)
- match actual restaurant business hours format for end user (needs market research)
  - see: Superette corvallis on google maps mobile (e.g. "8 am to 2 am")

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