#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SOURCE_ROOT="${1:-$REPO_ROOT/app/src/main/java}"
BASE_PACKAGE="de.hamedtanha.servertoolkit"
FEATURE_PREFIX="$BASE_PACKAGE.feature."

if [[ ! -d "$SOURCE_ROOT" ]]; then
  echo "Architecture dependency source root does not exist: $SOURCE_ROOT" >&2
  exit 2
fi

violations=0
checked_files=0

report_violation() {
  local rule_id="$1"
  local source_file="$2"
  local import_name="$3"
  local detail="$4"

  printf 'ARCHITECTURE VIOLATION [%s] %s\n' "$rule_id" "$source_file" >&2
  printf '  import %s\n' "$import_name" >&2
  printf '  %s\n' "$detail" >&2
  violations=$((violations + 1))
}

is_named_exception() {
  local source_file="$1"
  local import_name="$2"

  case "$source_file|$import_name" in
    "feature/ssh/presentation/screen/SshScreen.kt|$BASE_PACKAGE.feature.ssh.data.source.AndroidSshPrivateKeySourceFactory")
      return 0
      ;;
    "feature/ssh/data/local/entity/SshTrustedHostKeyEntity.kt|$BASE_PACKAGE.feature.serverinventory.data.local.entity.ServerEntity")
      return 0
      ;;
    "feature/ssh/data/local/entity/SshConnectionHistoryEntity.kt|$BASE_PACKAGE.feature.serverinventory.data.local.entity.ServerEntity")
      return 0
      ;;
  esac

  return 1
}

while IFS= read -r -d '' file; do
  checked_files=$((checked_files + 1))
  relative_path="${file#"$SOURCE_ROOT"/}"

  while IFS= read -r line; do
    case "$line" in
      import\ *)
        import_name="${line#import }"
        import_name="${import_name%% as *}"
        ;;
      *)
        continue
        ;;
    esac

    if is_named_exception "$relative_path" "$import_name"; then
      continue
    fi

    case "$relative_path" in
      */domain/*)
        if [[ "$import_name" == android.* || "$import_name" == androidx.* ]]; then
          report_violation \
            "DOMAIN_PLATFORM" \
            "$relative_path" \
            "$import_name" \
            "Domain code must remain independent of Android and AndroidX APIs."
          continue
        fi

        if [[ "$import_name" == net.schmizz.sshj.* ]]; then
          report_violation \
            "DOMAIN_TRANSPORT" \
            "$relative_path" \
            "$import_name" \
            "Domain code must not depend on the SSHJ transport implementation."
          continue
        fi

        if [[ "$import_name" == "$BASE_PACKAGE".* && ( "$import_name" == *".data."* || "$import_name" == *".presentation."* ) ]]; then
          report_violation \
            "DOMAIN_LAYER" \
            "$relative_path" \
            "$import_name" \
            "Domain code may depend on project-owned domain/core contracts, not data or presentation implementations."
          continue
        fi
        ;;
    esac

    case "$relative_path" in
      feature/*/presentation/*)
        source_feature="${relative_path#feature/}"
        source_feature="${source_feature%%/*}"

        if [[ "$import_name" == androidx.room.* ]]; then
          report_violation \
            "PRESENTATION_ROOM" \
            "$relative_path" \
            "$import_name" \
            "Presentation must not depend on Room APIs."
          continue
        fi

        if [[ "$import_name" == "$FEATURE_PREFIX"* ]]; then
          target_path="${import_name#"$FEATURE_PREFIX"}"
          target_feature="${target_path%%.*}"

          if [[ "$target_feature" == "$source_feature" ]]; then
            if [[ "$import_name" == *".data."* || "$import_name" == *".di."* ]]; then
              report_violation \
                "PRESENTATION_IMPLEMENTATION" \
                "$relative_path" \
                "$import_name" \
                "Presentation must not depend on feature data/DI implementations outside an explicitly named composition exception."
              continue
            fi
          else
            if [[ "$import_name" == *".presentation."* || "$import_name" == *".data."* || "$import_name" == *".di."* ]]; then
              report_violation \
                "PRESENTATION_CROSS_FEATURE" \
                "$relative_path" \
                "$import_name" \
                "Cross-feature presentation dependencies must use stable project-owned domain contracts or app-level navigation boundaries."
              continue
            fi
          fi
        fi
        ;;
    esac

    case "$relative_path" in
      feature/*/data/*)
        source_feature="${relative_path#feature/}"
        source_feature="${source_feature%%/*}"

        if [[ "$import_name" == "$FEATURE_PREFIX"* ]]; then
          target_path="${import_name#"$FEATURE_PREFIX"}"
          target_feature="${target_path%%.*}"

          if [[ "$import_name" == *".presentation."* ]]; then
            report_violation \
              "DATA_PRESENTATION" \
              "$relative_path" \
              "$import_name" \
              "Data code must not depend on presentation code."
            continue
          fi

          if [[ "$target_feature" != "$source_feature" && "$import_name" == *".data."* ]]; then
            report_violation \
              "DATA_CROSS_FEATURE" \
              "$relative_path" \
              "$import_name" \
              "Cross-feature data implementation dependencies are forbidden outside explicitly named Room metadata exceptions."
            continue
          fi
        fi
        ;;
    esac
  done < "$file"
done < <(find "$SOURCE_ROOT" -type f -name '*.kt' -print0 | sort -z)

if (( violations > 0 )); then
  printf 'Architecture dependency check failed: %d violation(s) across %d production Kotlin file(s).\n' \
    "$violations" "$checked_files" >&2
  exit 1
fi

printf 'Architecture dependency check passed: %d production Kotlin file(s) checked.\n' "$checked_files"
