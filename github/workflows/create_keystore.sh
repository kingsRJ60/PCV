#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# PCV Keystore Generator
# Run this ONCE on your computer to create a signing keystore.
# Then upload the result to GitHub Secrets.
#
# Requirements: Java JDK installed (keytool comes with it)
# ─────────────────────────────────────────────────────────────────────────────

echo "=== PCV Keystore Generator ==="
echo ""

# Settings — change these to your own values
KEYSTORE_FILE="pcv_release.jks"
ALIAS="pcv_key"
STORE_PASS="your_store_password_here"    # Change this!
KEY_PASS="your_key_password_here"        # Change this!
VALIDITY=10000                            # days (~27 years)

# Generate the keystore
keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity $VALIDITY \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=PCV App, OU=Development, O=PCV, L=Unknown, S=Unknown, C=US"

echo ""
echo "=== Keystore created: $KEYSTORE_FILE ==="
echo ""
echo "Now convert it to base64 for GitHub Secrets:"
echo ""

# Convert to base64
base64 -i "$KEYSTORE_FILE" > pcv_keystore_base64.txt
echo "Base64 saved to: pcv_keystore_base64.txt"
echo ""
echo "=== Add these 4 GitHub Secrets ==="
echo "Go to: your repo → Settings → Secrets and variables → Actions → New secret"
echo ""
echo "Secret 1 name:  KEYSTORE_BASE64"
echo "Secret 1 value: (copy entire contents of pcv_keystore_base64.txt)"
echo ""
echo "Secret 2 name:  KEYSTORE_PASSWORD"
echo "Secret 2 value: $STORE_PASS"
echo ""
echo "Secret 3 name:  KEY_ALIAS"
echo "Secret 3 value: $ALIAS"
echo ""
echo "Secret 4 name:  KEY_PASSWORD"
echo "Secret 4 value: $KEY_PASS"
