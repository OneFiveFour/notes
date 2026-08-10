#!/usr/bin/env bash
set -euo pipefail

target="${1:-android}"
shift || true

clean=false
skip_tests=false
unsigned_android=false

for arg in "$@"; do
  case "$arg" in
    --clean) clean=true ;;
    --skip-tests) skip_tests=true ;;
    --unsigned-android) unsigned_android=true ;;
    *)
      echo "Unknown option: $arg" >&2
      exit 2
      ;;
  esac
done

run_gradle() {
  local tasks=("$@")
  if [[ "$clean" == true ]]; then
    tasks=("clean" "${tasks[@]}")
  fi

  echo "Running: ./gradlew ${tasks[*]}"
  ./gradlew "${tasks[@]}"
}

ensure_android_signing() {
  if [[ "$unsigned_android" == true ]]; then
    echo "Warning: building an unsigned Android release. Do not distribute this artifact." >&2
    return
  fi

  if [[ ! -f "keystore.properties" ]]; then
    echo "keystore.properties is missing. Copy keystore.properties.example, fill it in, or pass --unsigned-android for local validation only." >&2
    exit 1
  fi
}

quality_tasks=()
if [[ "$skip_tests" == false ]]; then
  quality_tasks=(":composeApp:jvmTest")
fi

android_tasks=(":composeApp:assembleRelease" ":composeApp:bundleRelease")
desktop_tasks=(":composeApp:packageReleaseDistributionForCurrentOS")
js_tasks=(":composeApp:jsBrowserDistribution")
wasm_tasks=(":composeApp:wasmJsBrowserDistribution")

case "$target" in
  android)
    ensure_android_signing
    run_gradle "${quality_tasks[@]}" "${android_tasks[@]}"
    ;;
  desktop)
    run_gradle "${quality_tasks[@]}" "${desktop_tasks[@]}"
    ;;
  js)
    run_gradle "${quality_tasks[@]}" "${js_tasks[@]}"
    ;;
  wasm)
    run_gradle "${quality_tasks[@]}" "${wasm_tasks[@]}"
    ;;
  web)
    run_gradle "${quality_tasks[@]}" "${js_tasks[@]}" "${wasm_tasks[@]}"
    ;;
  all)
    ensure_android_signing
    run_gradle "${quality_tasks[@]}" "${android_tasks[@]}" "${desktop_tasks[@]}" "${js_tasks[@]}" "${wasm_tasks[@]}"
    ;;
  *)
    echo "Usage: ./release-build.sh [android|desktop|web|js|wasm|all] [--clean] [--skip-tests] [--unsigned-android]" >&2
    exit 2
    ;;
esac
