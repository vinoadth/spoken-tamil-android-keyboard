# Standard Spoken Tamil Keyboard

An Android on-screen keyboard for typing Tamil in the **Standard Spoken Tamil (SST)**
romanization. The layout is a port of the X11 `in(tam_sst)` keyboard layout.

## Keyboard layout

### Lower case

```
 1   2   3   4   5   6   7   8   9   0
 ǹ   ù   e   r   t   y   u   i   o   p
   a   s   d   f   g   h   j   k   l
 ⇧   ǯ   à   ĉ   v   b   n   m   ⌫
?123  🌐  ,  [     space     ]  .  ⏎
```

### Upper case

```
 !   @   #   $   %   ^   &   *   (   )
 ņ   è   é   ŗ   ţ   ì   ú   í   ó   p
   á   ş   ḑ   f   ğ   h   ñ   k   ļ
 ⇧   z   æ   ĉ   v   b   ň   ŋ   ⌫
?123  🌐  ,  [     space     ]  .  ⏎
```

## Using the keyboard

- **⇧ Shift**: tap once for a single upper-case letter; double-tap for caps lock.
- **Long-press** a letter to pick any of its characters, including the plain
  ASCII letter shown in the key's corner.
- **🌐 Globe**: tap to return to your previous keyboard; long-press to choose
  another keyboard. Hidden when no other keyboard is enabled.
- **?123**: numbers and symbols.
- **⌫ Backspace**: hold to delete repeatedly.

## Setup

1. Install the app and open **Standard Spoken Tamil Keyboard**.
2. Tap **Enable keyboard** and turn on *Tamil (Standard Spoken Tamil)*.
3. Tap **Select keyboard** and choose it.
4. Try it in the text field on the same screen.

## Building

Requires Android Studio (or the Android SDK) and JDK 17+. The app supports
Android 8.0 (API 26) and later.

```bash
./gradlew assembleDebug        # build the APK
./gradlew testDebugUnitTest    # run unit tests
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## License

[MIT](LICENSE) © 2026 Vinoth G <vinoth@mail.ru>
