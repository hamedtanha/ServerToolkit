#!/usr/bin/env bash

set -Eeuo pipefail
IFS=$'\n\t'
umask 077

readonly SCRIPT_DIR="$(
    cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &&
        pwd -P
)"
readonly REPOSITORY_ROOT="$(
    cd -- "$SCRIPT_DIR/../.." &&
        pwd -P
)"

readonly RELEASE_CONFIG="$REPOSITORY_ROOT/config/release/android-release.properties"
readonly APP_BUILD_FILE="$REPOSITORY_ROOT/app/build.gradle.kts"
readonly CERTIFICATE_FINGERPRINT_FILE="$REPOSITORY_ROOT/config/release/android-signing-certificate.sha256"
readonly DEFAULT_KEYSTORE="$HOME/.servertoolkit/release-signing/servertoolkit-release.p12"
readonly DEFAULT_KEY_ALIAS="servertoolkit-release"

MODE="official"
TEMP_DIR=""

fail() {
    printf 'ERROR: %s\n' "$*" >&2
    exit 1
}

cleanup() {
    unset SERVERTOOLKIT_RELEASE_STORE_PASSWORD
    unset SERVERTOOLKIT_RELEASE_KEY_PASSWORD

    if [[ -n "$TEMP_DIR" && -d "$TEMP_DIR" ]]; then
        rm -rf -- "$TEMP_DIR"
    fi
}

trap cleanup EXIT
trap 'fail "Release signing failed at line $LINENO."' ERR

usage() {
    cat <<'EOF'
Usage:
  scripts/release/sign-android-apk.sh
  scripts/release/sign-android-apk.sh --validation

Modes:
  official
      Requires a clean main branch aligned with origin/main.
      Preserves the verified APK, checksum, and release evidence.

  --validation
      Allows a clean non-main branch.
      Runs the complete signing workflow but deletes the signed
      validation artifact afterward.

Optional environment variables:
  SERVERTOOLKIT_RELEASE_KEYSTORE
  SERVERTOOLKIT_RELEASE_KEY_ALIAS
  SERVERTOOLKIT_RELEASE_STORE_PASSWORD
  SERVERTOOLKIT_RELEASE_KEY_PASSWORD
  SERVERTOOLKIT_RELEASE_RECOVERY_VERIFIED=YES
EOF
}

case "${1:-}" in
    "")
        ;;
    --validation)
        MODE="validation"
        ;;
    -h|--help)
        usage
        exit 0
        ;;
    *)
        usage >&2
        fail "Unsupported argument: $1"
        ;;
esac

[[ "$#" -le 1 ]] || fail "Only one optional argument is supported."

require_command() {
    command -v "$1" >/dev/null 2>&1 ||
        fail "Required command is unavailable: $1"
}

read_property() {
    local key="$1"
    local file="$2"
    local count
    local value

    count="$(grep -c "^${key}=" "$file" || true)"

    [[ "$count" == "1" ]] ||
        fail "Expected exactly one '${key}' property in $file."

    value="$(sed -n "s/^${key}=//p" "$file")"

    [[ -n "$value" ]] ||
        fail "Property '${key}' must not be empty in $file."

    printf '%s' "$value"
}

normalize_sha256() {
    printf '%s' "$1" |
        tr '[:lower:]' '[:upper:]' |
        tr -d '[:space:]:'
}

resolve_android_sdk_root() {
    local sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"

    if [[ -z "$sdk_root" && -f "$REPOSITORY_ROOT/local.properties" ]]; then
        sdk_root="$(
            sed -n 's/^sdk\.dir=//p' "$REPOSITORY_ROOT/local.properties" |
                head -n 1 |
                sed 's/\\:/:/g; s/\\\\/\\/g'
        )"
    fi

    [[ -n "$sdk_root" ]] ||
        fail "Android SDK root could not be resolved."

    [[ -d "$sdk_root" ]] ||
        fail "Android SDK directory does not exist: $sdk_root"

    printf '%s' "$sdk_root"
}

extract_badging_value() {
    local key="$1"
    local package_line="$2"
    local value

    value="$(
        printf '%s\n' "$package_line" |
            sed -n "s/.*[[:space:]]${key}='\([^']*\)'.*/\1/p"
    )"

    [[ -n "$value" ]] ||
        fail "Package metadata field was not found: $key"

    printf '%s' "$value"
}

for command_name in git grep sed tr awk shasum mktemp date tee find; do
    require_command "$command_name"
done

[[ -x "$REPOSITORY_ROOT/gradlew" ]] ||
    fail "Gradle Wrapper is not executable."

[[ -f "$RELEASE_CONFIG" ]] ||
    fail "Release configuration is missing: $RELEASE_CONFIG"

[[ -f "$APP_BUILD_FILE" ]] ||
    fail "Android application build file is missing: $APP_BUILD_FILE"

[[ -f "$CERTIFICATE_FINGERPRINT_FILE" ]] ||
    fail "Certificate fingerprint file is missing: $CERTIFICATE_FINGERPRINT_FILE"

cd -- "$REPOSITORY_ROOT"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 ||
    fail "Repository root is not a Git work tree."

[[ -z "$(git status --porcelain)" ]] ||
    fail "Working tree must be clean before signing."

git diff --check

readonly SOURCE_COMMIT="$(git rev-parse HEAD)"
readonly SOURCE_BRANCH="$(git branch --show-current)"

[[ -n "$SOURCE_BRANCH" ]] ||
    fail "Signing requires a named Git branch."

if [[ "$MODE" == "official" ]]; then
    [[ "$SOURCE_BRANCH" == "main" ]] ||
        fail "Official signing must run from main."

    [[ "${SERVERTOOLKIT_RELEASE_RECOVERY_VERIFIED:-}" == "YES" ]] ||
        fail \
            "Set SERVERTOOLKIT_RELEASE_RECOVERY_VERIFIED=YES after verifying the recovery copy."

    if git show-ref --verify --quiet refs/remotes/origin/main; then
        [[ "$SOURCE_COMMIT" == "$(git rev-parse origin/main)" ]] ||
            fail "Local main must match origin/main before official signing."
    fi
fi

readonly EXPECTED_APPLICATION_ID="$(
    read_property applicationId "$RELEASE_CONFIG"
)"
readonly EXPECTED_VERSION_CODE="$(
    read_property versionCode "$RELEASE_CONFIG"
)"
readonly EXPECTED_VERSION_NAME="$(
    read_property versionName "$RELEASE_CONFIG"
)"
readonly BUILD_TOOLS_VERSION="$(
    read_property buildToolsVersion "$RELEASE_CONFIG"
)"
readonly NDK_VERSION="$(
    read_property ndkVersion "$RELEASE_CONFIG"
)"
readonly EXPECTED_CERTIFICATE_SHA256="$(
    normalize_sha256 "$(cat "$CERTIFICATE_FINGERPRINT_FILE")"
)"

[[ "$EXPECTED_VERSION_CODE" =~ ^[0-9]+$ ]] ||
    fail "Configured versionCode is invalid."

readonly EXPECTED_GRADLE_NDK_ASSIGNMENT="    ndkVersion = \"$NDK_VERSION\""
readonly GRADLE_NDK_ASSIGNMENT_COUNT="$(
    grep -Fxc \
        "$EXPECTED_GRADLE_NDK_ASSIGNMENT" \
        "$APP_BUILD_FILE" ||
        true
)"

[[ "$GRADLE_NDK_ASSIGNMENT_COUNT" == "1" ]] ||
    fail \
        "Release NDK version does not match the Gradle Android module."

[[ "$EXPECTED_CERTIFICATE_SHA256" =~ ^[0-9A-F]{64}$ ]] ||
    fail "Configured certificate SHA-256 fingerprint is invalid."

readonly SDK_ROOT="$(resolve_android_sdk_root)"
readonly BUILD_TOOLS="$SDK_ROOT/build-tools/$BUILD_TOOLS_VERSION"
readonly NDK_ROOT="$SDK_ROOT/ndk/$NDK_VERSION"
readonly NDK_LLVM_PREBUILT="$NDK_ROOT/toolchains/llvm/prebuilt"
readonly APKSIGNER="$BUILD_TOOLS/apksigner"
readonly ZIPALIGN="$BUILD_TOOLS/zipalign"
readonly AAPT2="$BUILD_TOOLS/aapt2"

for tool in "$APKSIGNER" "$ZIPALIGN" "$AAPT2"; do
    [[ -x "$tool" ]] ||
        fail "Required Android build tool is missing: $tool"
done

[[ -d "$NDK_ROOT" ]] ||
    fail "Required Android NDK is missing: $NDK_ROOT"

[[ -d "$NDK_LLVM_PREBUILT" ]] ||
    fail "Required Android NDK LLVM toolchain is missing: $NDK_LLVM_PREBUILT"

readonly LLVM_STRIP="$(
    find "$NDK_LLVM_PREBUILT" \
        \( -type f -o -type l \) \
        -path '*/bin/llvm-strip' \
        -print |
        sed -n '1p'
)"

[[ -x "$LLVM_STRIP" ]] ||
    fail "Required Android NDK llvm-strip executable is missing."

readonly KEYSTORE="${SERVERTOOLKIT_RELEASE_KEYSTORE:-$DEFAULT_KEYSTORE}"
readonly KEY_ALIAS="${SERVERTOOLKIT_RELEASE_KEY_ALIAS:-$DEFAULT_KEY_ALIAS}"

[[ -f "$KEYSTORE" ]] ||
    fail "Release keystore was not found: $KEYSTORE"

[[ -r "$KEYSTORE" ]] ||
    fail "Release keystore is not readable: $KEYSTORE"

[[ -n "$KEY_ALIAS" ]] ||
    fail "Release key alias must not be empty."

readonly KEYSTORE_DIRECTORY="$(
    cd -- "$(dirname -- "$KEYSTORE")" &&
        pwd -P
)"
readonly KEYSTORE_PATH="$KEYSTORE_DIRECTORY/$(basename -- "$KEYSTORE")"

case "$KEYSTORE_PATH" in
    "$REPOSITORY_ROOT"|"$REPOSITORY_ROOT"/*)
        fail "Release keystore must remain outside the Git repository."
        ;;
esac

if [[ -z "${SERVERTOOLKIT_RELEASE_STORE_PASSWORD:-}" ]]; then
    read \
        -r \
        -s \
        -p "Release keystore password: " \
        SERVERTOOLKIT_RELEASE_STORE_PASSWORD \
        </dev/tty

    printf '\n' >/dev/tty
fi

[[ -n "$SERVERTOOLKIT_RELEASE_STORE_PASSWORD" ]] ||
    fail "Release keystore password must not be empty."

export SERVERTOOLKIT_RELEASE_STORE_PASSWORD

if [[ -z "${SERVERTOOLKIT_RELEASE_KEY_PASSWORD:-}" ]]; then
    SERVERTOOLKIT_RELEASE_KEY_PASSWORD="$SERVERTOOLKIT_RELEASE_STORE_PASSWORD"
fi

[[ -n "$SERVERTOOLKIT_RELEASE_KEY_PASSWORD" ]] ||
    fail "Release key password must not be empty."

export SERVERTOOLKIT_RELEASE_KEY_PASSWORD

readonly UNSIGNED_APK="$REPOSITORY_ROOT/app/build/outputs/apk/release/app-release-unsigned.apk"
readonly OUTPUT_METADATA="$REPOSITORY_ROOT/app/build/outputs/apk/release/output-metadata.json"
readonly OUTPUT_DIRECTORY="$REPOSITORY_ROOT/build/release"
readonly OUTPUT_APK="$OUTPUT_DIRECTORY/ServerToolkit-v${EXPECTED_VERSION_NAME}.apk"
readonly OUTPUT_CHECKSUM="$OUTPUT_APK.sha256"
readonly OUTPUT_EVIDENCE="$OUTPUT_DIRECTORY/ServerToolkit-v${EXPECTED_VERSION_NAME}-release-evidence.txt"

if [[ "$MODE" == "official" ]]; then
    for output_file in \
        "$OUTPUT_APK" \
        "$OUTPUT_CHECKSUM" \
        "$OUTPUT_EVIDENCE"
    do
        [[ ! -e "$output_file" ]] ||
            fail "Refusing to overwrite existing release output: $output_file"
    done
fi

TEMP_DIR="$(
    mktemp -d "${TMPDIR:-/tmp}/servertoolkit-release.XXXXXX"
)"

readonly ALIGNED_APK="$TEMP_DIR/app-release-aligned.apk"
readonly SIGNED_APK="$TEMP_DIR/ServerToolkit-v${EXPECTED_VERSION_NAME}.apk"
readonly VERIFY_LOG="$TEMP_DIR/apksigner-verify.txt"

printf 'Building unsigned release APK from commit %s...\n' "$SOURCE_COMMIT"

./gradlew clean :app:assembleRelease --stacktrace

[[ -f "$UNSIGNED_APK" ]] ||
    fail "Unsigned release APK was not produced."

[[ -f "$OUTPUT_METADATA" ]] ||
    fail "Gradle output metadata was not produced."

if "$APKSIGNER" verify "$UNSIGNED_APK" >/dev/null 2>&1; then
    fail "Gradle release output is unexpectedly signed."
fi

printf 'Aligning APK with Android Build Tools %s...\n' \
    "$BUILD_TOOLS_VERSION"

"$ZIPALIGN" \
    -P 16 \
    -f \
    4 \
    "$UNSIGNED_APK" \
    "$ALIGNED_APK"

"$ZIPALIGN" \
    -c \
    -P 16 \
    4 \
    "$ALIGNED_APK"

printf 'Signing aligned APK...\n'

"$APKSIGNER" sign \
    --ks "$KEYSTORE_PATH" \
    --ks-key-alias "$KEY_ALIAS" \
    --ks-pass env:SERVERTOOLKIT_RELEASE_STORE_PASSWORD \
    --key-pass env:SERVERTOOLKIT_RELEASE_KEY_PASSWORD \
    --out "$SIGNED_APK" \
    "$ALIGNED_APK"

printf 'Verifying APK signature and certificate...\n'

"$APKSIGNER" verify \
    --verbose \
    --print-certs \
    -Werr \
    "$SIGNED_APK" \
    2>&1 |
    tee "$VERIFY_LOG"

"$ZIPALIGN" \
    -c \
    -P 16 \
    4 \
    "$SIGNED_APK"

if grep -Fqi 'CN=Android Debug' "$VERIFY_LOG"; then
    fail "Signed APK uses an Android debug certificate."
fi

readonly SIGNER_COUNT_LINE_COUNT="$(
    grep -c \
        '^Number of signers:' \
        "$VERIFY_LOG" ||
        true
)"

[[ "$SIGNER_COUNT_LINE_COUNT" == "1" ]] ||
    fail "Expected exactly one APK signer-count result."

readonly ACTUAL_SIGNER_COUNT="$(
    sed -n \
        's/^Number of signers:[[:space:]]*//p' \
        "$VERIFY_LOG"
)"

[[ "$ACTUAL_SIGNER_COUNT" =~ ^[0-9]+$ ]] ||
    fail "APK signer count is invalid."

[[ "$ACTUAL_SIGNER_COUNT" == "1" ]] ||
    fail "Expected exactly one APK signer, found $ACTUAL_SIGNER_COUNT."

readonly CERTIFICATE_LINE_COUNT="$(
    grep -c \
        '^Signer #1 certificate SHA-256 digest:' \
        "$VERIFY_LOG" ||
        true
)"

[[ "$CERTIFICATE_LINE_COUNT" == "1" ]] ||
    fail "Expected exactly one signer certificate SHA-256 digest."

readonly ACTUAL_CERTIFICATE_SHA256="$(
    normalize_sha256 "$(
        sed -n \
            's/^Signer #1 certificate SHA-256 digest:[[:space:]]*//p' \
            "$VERIFY_LOG"
    )"
)"

[[ "$ACTUAL_CERTIFICATE_SHA256" == "$EXPECTED_CERTIFICATE_SHA256" ]] ||
    fail \
        "Signing certificate fingerprint does not match the accepted release identity."

printf 'Verifying application metadata...\n'

readonly BADGING_OUTPUT="$(
    "$AAPT2" dump badging "$SIGNED_APK"
)"
readonly PACKAGE_LINE="$(
    printf '%s\n' "$BADGING_OUTPUT" |
        sed -n '1p'
)"
readonly ACTUAL_APPLICATION_ID="$(
    extract_badging_value name "$PACKAGE_LINE"
)"
readonly ACTUAL_VERSION_CODE="$(
    extract_badging_value versionCode "$PACKAGE_LINE"
)"
readonly ACTUAL_VERSION_NAME="$(
    extract_badging_value versionName "$PACKAGE_LINE"
)"

[[ "$ACTUAL_APPLICATION_ID" == "$EXPECTED_APPLICATION_ID" ]] ||
    fail \
        "Application ID mismatch: expected $EXPECTED_APPLICATION_ID, found $ACTUAL_APPLICATION_ID"

[[ "$ACTUAL_VERSION_CODE" == "$EXPECTED_VERSION_CODE" ]] ||
    fail \
        "Version code mismatch: expected $EXPECTED_VERSION_CODE, found $ACTUAL_VERSION_CODE"

[[ "$ACTUAL_VERSION_NAME" == "$EXPECTED_VERSION_NAME" ]] ||
    fail \
        "Version name mismatch: expected $EXPECTED_VERSION_NAME, found $ACTUAL_VERSION_NAME"

readonly APK_SHA256="$(
    shasum -a 256 "$SIGNED_APK" |
        awk '{ print toupper($1) }'
)"
readonly GENERATED_AT_UTC="$(
    date -u '+%Y-%m-%dT%H:%M:%SZ'
)"

[[ "$APK_SHA256" =~ ^[0-9A-F]{64}$ ]] ||
    fail "Generated APK SHA-256 checksum is invalid."

if [[ "$MODE" == "validation" ]]; then
    cat <<EOF

VALIDATION SUCCESSFUL

The complete post-build signing workflow passed.
The signed validation artifact will now be deleted and must not be distributed.

Source branch:       $SOURCE_BRANCH
Source commit:       $SOURCE_COMMIT
Application ID:      $ACTUAL_APPLICATION_ID
Version code:        $ACTUAL_VERSION_CODE
Version name:        $ACTUAL_VERSION_NAME
NDK version:         $NDK_VERSION
Certificate SHA-256: $ACTUAL_CERTIFICATE_SHA256
APK SHA-256:         $APK_SHA256
EOF

    exit 0
fi

mkdir -p -- "$OUTPUT_DIRECTORY"

cp -- "$SIGNED_APK" "$OUTPUT_APK"
chmod 644 "$OUTPUT_APK"

printf '%s  %s\n' \
    "$APK_SHA256" \
    "$(basename -- "$OUTPUT_APK")" \
    > "$OUTPUT_CHECKSUM"

chmod 644 "$OUTPUT_CHECKSUM"

cat > "$OUTPUT_EVIDENCE" <<EOF
project=ServerToolkit
releaseStatus=verified-local-release-artifact
generatedAtUtc=$GENERATED_AT_UTC
sourceBranch=$SOURCE_BRANCH
sourceCommit=$SOURCE_COMMIT
applicationId=$ACTUAL_APPLICATION_ID
versionCode=$ACTUAL_VERSION_CODE
versionName=$ACTUAL_VERSION_NAME
buildToolsVersion=$BUILD_TOOLS_VERSION
ndkVersion=$NDK_VERSION
certificateSha256=$ACTUAL_CERTIFICATE_SHA256
apkSha256=$APK_SHA256
artifactFile=$(basename -- "$OUTPUT_APK")
recoveryVerifiedByMaintainer=true
EOF

chmod 644 "$OUTPUT_EVIDENCE"

cat <<EOF

OFFICIAL RELEASE ARTIFACT CREATED AND VERIFIED

Artifact:            $OUTPUT_APK
Checksum:            $OUTPUT_CHECKSUM
Release evidence:    $OUTPUT_EVIDENCE
Source commit:        $SOURCE_COMMIT
Certificate SHA-256: $ACTUAL_CERTIFICATE_SHA256
APK SHA-256:         $APK_SHA256
EOF
