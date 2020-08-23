# Camera11
Camera intent for Android 11

Since Android 11, it's no longer possible to fire a image-capture Intent, and expect an IntentChooser to pop up automatically.
For reasons, Google decided that it's better to always launch the pre-install camera app, instead of let the user have choice.
To make it harder for developer to give this choice back to the user, they've crippled the IntentResolver, to don't return any results when querying.

**This library gives that choice back to the user.**
We just work around the whole IntentResolver, and read the AndroidManifest.xml's directly, to find out which IntentFilters there are.

