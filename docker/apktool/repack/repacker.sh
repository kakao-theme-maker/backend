#!/bin/bash
set -euo pipefail

KEYSTORE_FILE="./key.jks"
ALIAS_NAME="theme"
KEYSTORE_PASSWORD="kakaothemepassword1234"
KEY_PASSWORD="kakaothemepassword1234"

echo "[1/5] Optimizing general PNGs with OptiPNG..."
find "/input/res" -type f -name "*.png" ! -name "*.9.png" | \
xargs -n 1 -P 4 -I {} sh -c '
  echo "Optimizing {}..."
  optipng -o2 "{}" || { echo "OptiPNG failed for {}"; exit 1; }
'

echo "[2/5] Optimizing 9-patch PNGs with pngcrush..."
find "/input/res" -type f -name "*.9.png" | \
xargs -n 1 -P 4 -I {} sh -c '
  echo "Crushing {}..."
  TMP_FILE="{}.tmp"
  pngcrush -rem alla -reduce "{}" "$TMP_FILE" || { echo "pngcrush failed for {}"; exit 1; }
  mv "$TMP_FILE" "{}"
'

echo "[3/5] Building APK with apktool..."
./apktool -f b /input -o /output/output-unsigned.apk --use-aapt2 || { echo "APK build failed"; exit 1; }

# APK가 완전히 생성될 때까지 대기
echo "Waiting for unsigned APK..."
while [ ! -f /output/output-unsigned.apk ]; do
    sleep 0.5
done

echo "[4/5] Zipaligning APK..."
zipalign -p -f -v 4 /output/output-unsigned.apk /output/output-signed.apk || { echo "Zipalign failed"; exit 1; }

# Zipalign 완료 확인
while [ ! -f /output/output-signed.apk ]; do
    sleep 0.5
done

echo "[5/5] Signing APK..."
apksigner sign --verbose \
    --ks "$KEYSTORE_FILE" \
    --ks-pass pass:"$KEYSTORE_PASSWORD" \
    --key-pass pass:"$KEY_PASSWORD" \
    --ks-key-alias "$ALIAS_NAME" \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    /output/output-signed.apk || { echo "APK signing failed"; exit 1; }

# sign 완료 확인
while [ ! -f /output/output-signed.apk ]; do
    sleep 0.5
done

echo "Verifying APK..."
apksigner verify --verbose --print-certs /output/output-signed.apk || { echo "APK verification failed"; exit 1; }

echo "APK build & sign completed successfully."
