KEYSTORE_FILE="./keystore.jks"
ALIAS_NAME="theme"
KEYSTORE_PASSWORD="kakaothemepassword1234"
KEY_PASSWORD="kakaothemepassword1234"

zipalign -p -f -v 4 "$1" result.apk

# APK 서명 (V2 서명)
echo "Signing APK with V2 signatures..."
apksigner sign --verbose --ks "$KEYSTORE_FILE" \
    --ks-pass pass:"$KEYSTORE_PASSWORD" \
    --key-pass pass:"$KEY_PASSWORD" \
    --ks-key-alias "$ALIAS_NAME" \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    result.apk

# 서명된 APK 확인
echo "Verifying APK..."
apksigner verify --verbose result.apk