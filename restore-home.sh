#!/usr/bin/env bash
# Brings back the stock Google TV launcher and makes it the Home screen again.
# TV address comes from local.properties (tv.ip=HOST:5555), which is gitignored.
TV=$(grep -E '^tv\.ip=' "$(dirname "$0")/local.properties" 2>/dev/null | cut -d= -f2)
[ -n "$TV" ] || { echo "Set tv.ip=<TV address>:5555 in local.properties (see local.properties.example)"; exit 1; }
adb connect "$TV" >/dev/null
adb -s "$TV" shell pm enable com.google.android.apps.tv.launcherx
adb -s "$TV" shell pm enable com.google.android.tungsten.setupwraith
adb -s "$TV" shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity
echo "Current Home:"
adb -s "$TV" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
