#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# Taps can't be timed reliably through adb, so the sample opens each screen from the `scene` extra, with a
# message or note preselected and the debug overlay (size class and posture) on, so every capture is the same.
# Every capture is checked for the expected text and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: list-detail and supporting pane, light and dark
#   bash scripts/screenshots.sh phone    # pixel_7: the compact inbox with the bottom bar, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it.
# "Thread details" only appears when the third pane is visible, "Related notes" only with the supporting pane.
expected_text() {
  case "$1" in
    listdetail) echo "Thread details" ;;
    supporting) echo "Related notes" ;;
    compact) echo "Inbox" ;;
  esac
}

suffix() {
  if [ "$1" = dark ]; then echo "-dark"; else echo ""; fi
}

install_sample
if [ "$device" = tablet ]; then
  ensure_landscape
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in listdetail supporting; do
      fresh_launch --es scene "$scene"
      capture "tablet-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    fresh_launch --es scene compact
    capture "phone-compact$(suffix "$mode")" "$(expected_text compact)"
  done
fi
