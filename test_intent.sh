#!/bin/bash
adb shell am start -a android.car.intent.action.MEDIA_TEMPLATE -e android.car.intent.extra.MEDIA_COMPONENT com.example.musicplayer/.MusicBrowserService
