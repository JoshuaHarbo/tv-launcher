#!/usr/bin/env bash
# Makes com.josh.tvhome the Home screen by disabling the stock Google TV launcher.
# Undo with ./restore-home.sh. Add --with-setupwraith only if Home still opens the wrong screen.
# TV address comes from local.properties (tv.ip=HOST:5555), which is gitignored.
TV=$(grep -E '^tv\.ip=' "$(dirname "$0")/local.properties" 2>/dev/null | cut -d= -f2)
[ -n "$TV" ] || { echo "Set tv.ip=<TV address>:5555 in local.properties (see local.properties.example)"; exit 1; }
adb connect "$TV" >/dev/null
adb -s "$TV" shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
if [ "$1" = "--with-setupwraith" ]; then
  adb -s "$TV" shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
fi
# The preferred-home choice is tied to the set of home apps, so set it again after the change.
adb -s "$TV" shell cmd package set-home-activity com.josh.tvhome/.MainActivity
echo "Current Home:"
adb -s "$TV" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
echo "Expect com.josh.tvhome/.MainActivity above. Press Home on the remote to confirm."
