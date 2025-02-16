# Media Provider Manager

An Xposed module intended to prevent media storage abuse.

[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/MediaProviderManager)
[![Stars](https://img.shields.io/github/stars/GuhDoy/Media-Provider-Manager?label=Stars)](https://github.com/MaterialCleaner/Media-Provider-Manager)
[![Download](https://img.shields.io/github/v/release/GuhDoy/Media-Provider-Manager?label=Download)](https://github.com/MaterialCleaner/Media-Provider-Manager/releases/latest)

## Screenshots

<p><img src="https://raw.githubusercontent.com/MaterialCleaner/Media-Provider-Manager/main/screenshots/about.jpg" height="400" alt="Screenshot"/>
<img src="https://raw.githubusercontent.com/MaterialCleaner/Media-Provider-Manager/main/screenshots/record.jpg" height="400" alt="Screenshot"/>
<img src="https://raw.githubusercontent.com/MaterialCleaner/Media-Provider-Manager/main/screenshots/template.jpg" height="400" alt="Screenshot"/></p>

## What is media store

[Media store][1] is an optimized index into media collections provided by the Android framework. When an application needs to access media files (e.g. an album application wants to display all the pictures in the device), it is more easier to [interact with the media store][2] than traversing all files in the external storage volume. In addition it reduces the number of files accessible to the app, which helps to protect user privacy.

## How media store is abused

As with native storage, Android does not offer a fine-grained management scheme for media storage.
- Apps only need low-risk permissions to access all media files, and users cannot limit the scope of reading.
- No permission is required for applications to insert files through the media store. Writing files freewheelingly will clutter up the external storage and the media store, and it can also be used for cross-application tracking.

## Features

- Media file manager (no root or Xposed needed, only requires [READ_EXTERNAL_STORAGE][3] permission)
- Filter data returned from the media store to protect your privacy.
- Prevent apps from freewheelingly writing files through the media store.
- Provide usage record to help you aware whether the application uses media store and whether there is any abuse.
- Scan file when it's inserted using obsolete API. If you have modified the file hierarchy in the mount namespace of the application processes (~~aka storage redirect~~), this feature allows the media store to be updated in time.
- Prevent 💩 ROM's download manager from creating non-standard files.
- Material Design 3, easy to use.
- Open source.

## Source code

[https://github.com/MaterialCleaner/Media-Provider-Manager](https://github.com/MaterialCleaner/Media-Provider-Manager)

## Releases

[Github Release](https://github.com/MaterialCleaner/Media-Provider-Manager/releases/latest)

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

[1]: https://developer.android.com/reference/android/provider/MediaStore
[2]: https://developer.android.com/training/data-storage/use-cases#handle-media-files
[3]: https://developer.android.com/reference/android/Manifest.permission.html#READ_EXTERNAL_STORAGE
