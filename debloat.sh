#!/usr/bin/env bash
# Usage: ./debloat.sh [tier1|tier2|all] [--apply|--restore]     (no flag = dry run)
#   tier1: uninstall for user 0 (data kept)      undo: cmd package install-existing
#   tier2: disable for user 0 (reversible)       undo: pm enable
# Do tier1 first, reboot, test. Do tier2 one package at a time (see README) after that.
# TV address comes from local.properties (tv.ip=HOST:5555), which is gitignored.
TV=$(grep -E '^tv\.ip=' "$(dirname "$0")/local.properties" 2>/dev/null | cut -d= -f2)
[ -n "$TV" ] || { echo "Set tv.ip=<TV address>:5555 in local.properties (see local.properties.example)"; exit 1; }

# Tier 1: promo apps and apps you do not use. High confidence.
TIER1=(
  com.tcl.tv.plus
  com.tcl.channelplus
  com.tcl.tv.tclchannel
  com.tubitv
  com.roku.web.trc
  com.google.android.play.games
)

# Tier 2: TCL and partner extras that are probably not needed. Medium to low confidence.
# Test the TV after each one: HDMI inputs, remote pairing, Wi-Fi, voice, app installs.
TIER2=(
  com.tcl.esticker
  com.tcl.smartalexa
  com.tcl.messagebox
  com.tcl.usercenter
  # com.tcl.ttvs   (kept: unclear purpose)
  # com.tcl.t_solo   (kept: unclear purpose)
  com.tcl.copydatatotv
  # com.tcl.ui_mediaCenter   (kept: USB media playback)
  android.autoinstalls.config.google.gtvpai
)

# Never touched on purpose, because TV functions or app installs depend on them:
#   com.google.android.apps.tv.launcherx (stock launcher, fallback), com.android.vending,
#   com.google.android.gms, com.google.android.gsf, com.google.android.webview,
#   com.google.android.packageinstaller, com.google.android.permissioncontroller,
#   com.google.android.katniss (voice), com.google.android.tv.remote.service,
#   com.tcl.tv, com.tcl.tvinput, com.tcl.settings, com.tcl.system.server, com.tcl.guard,
#   com.tcl.providers.config, com.tcl.initsetup, com.tcl.autopair, com.tcl.tcl_bt_rcu_service,
#   com.tcl.micmanager, com.tcl.common.shortcutmenu, com.tcl.dashboard, com.tcl.suspension,
#   com.tcl.partnercustomizer, com.tcl.android.webview, com.tcl.wifi.resources.overlay,
#   and everything under com.android.*

TIER=all; ACTION=dry
for a in "$@"; do
  case $a in
    tier1|tier2|all) TIER=$a ;;
    --apply) ACTION=apply ;;
    --restore) ACTION=restore ;;
  esac
done

adb connect "$TV" >/dev/null
doit() { echo "$@"; [ "$ACTION" = dry ] || adb -s "$TV" shell "${@:2}"; }

if [ "$TIER" = tier1 ] || [ "$TIER" = all ]; then
  for p in "${TIER1[@]}"; do
    case $ACTION in
      dry)     echo "[tier1] would uninstall: $p" ;;
      apply)   echo "[tier1] uninstalling: $p"; adb -s "$TV" shell pm uninstall -k --user 0 "$p" ;;
      restore) echo "[tier1] restoring: $p"; adb -s "$TV" shell cmd package install-existing "$p" ;;
    esac
  done
fi
if [ "$TIER" = tier2 ] || [ "$TIER" = all ]; then
  for p in "${TIER2[@]}"; do
    case $ACTION in
      dry)     echo "[tier2] would disable: $p" ;;
      apply)   echo "[tier2] disabling: $p"; adb -s "$TV" shell pm disable-user --user 0 "$p" ;;
      restore) echo "[tier2] enabling: $p"; adb -s "$TV" shell pm enable "$p" ;;
    esac
  done
fi
