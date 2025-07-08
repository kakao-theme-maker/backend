#!/bin/bash
KEYSTORE_FILE="./key.jks"
ALIAS_NAME="theme"
KEYSTORE_PASSWORD="kakaothemepassword1234"
KEY_PASSWORD="kakaothemepassword1234"

./apktool -f b /input -o /output/output-unsigned.apk --use-aapt2

# zipalign 정렬
zipalign -p -f -v 4 /output/output-unsigned.apk /output/output-signed.apk

# APK 서명 (V2 서명)
echo "Signing APK with V2 signatures..."
apksigner sign --verbose --ks "$KEYSTORE_FILE" \
    --ks-pass pass:"$KEYSTORE_PASSWORD" \
    --key-pass pass:"$KEY_PASSWORD" \
    --ks-key-alias "$ALIAS_NAME" \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    /output/output-signed.apk

# 서명된 APK 확인
echo "Verifying APK..."
apksigner verify --verbose --print-certs /output/output-signed.apk