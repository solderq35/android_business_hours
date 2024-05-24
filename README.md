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
  - assume my code should be able to handle other days that what is shown on the api endpoint, or that they have "hidden test cases"
- safe to reorder by day?
- all events happen same each week
- local timezone on user matches server
- am pm final format (confirmed by figma)
- match actual restaurant business hours format for end user (needs market research)
  - see: Superette corvallis on google maps mobile (e.g. "8 am to 2 am")
