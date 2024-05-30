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
- All days from API endpoint will be in "first 3 letters of day of week name" format (e.g. "TUE")
- All time of day from API endpoint will be in HH:mm:ss format (e.g. "07:00:00")
- For "Open until {time}, reopens at {next time block}", I only show "reopens at..." if the next time block is on the same day
- All events happen same each week (holidays etc out of scope)
- Local timezone on user matches server
- Caching requests from API endpoint is out of scope
- Didn't have time to implement refresh functionality, so recompile app if testing custom timestamps etc
- When in doubt, check how google maps mobile app does the UI

## Test Cases
Edit `val timestamp` in `app\src\main\java\com\example\businesshours\ui\screens\HomeScreen.kt`
See https://www.unixtimestamp.com/index.php to convert Unix timestamps for debug.
Below Unix timestamps were tested for PST.
-  see https://www.unixtimestamp.com/index.php to convert Unix for debug
-  1716973724 (wednesday 2:08am) -> wednesday 7am - 1pm, "opens again at 7 AM", red dot
-  1716970124 (wednesday 1:08am) -> tuesday 3pm - (wednesday) 2am, "Open until 2AM", yellow dot
-  1716998924 (wednesday 9:08am) -> wednesday 7am - 1pm, "Open until 1PM, reopens at 3PM", green
-  dot
-  1717013324 (wednesday 1:08pm) -> Wednesday 3pm - 10pm, "Opens again at 12 AM", red dot
-  1717042085 (wednesday 9:08 pm) -> wednesday 3pm - 10pm, "Open until 10PM", yellow dot
-  1717049324 (wednesday 11:08pm) -> thursday 24h, "Opens again at 7 M", red dot
-  1717023600 (wednesday 4:00pm) -> wednesday 3pm - 10pm, "Open until 10 PM", green dot
-  1717196400 (Friday 4:00pm) -> N/A (no time block for Friday), "Opens Tuesday 7 AM", red dot

## Screenshot
![image](https://github.com/solderq35/android_business_hours/assets/82061589/2bde7ea7-5440-41eb-85ac-0d170b24bce6)