#!/usr/bin/env bash

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# do not exit when any command fails (issue with git flow)
set +e

appName="Tchap Android"

printf "\n================================================================================\n"
printf "|             Welcome to the Tchap release script (V2)!                        |\n"
printf "================================================================================\n"

printf "Checking environment...\n"
envError=0

# Check that bundletool is installed
if ! command -v bundletool &> /dev/null
then
    printf "Fatal: bundletool is not installed. You can install it running \`brew install bundletool\`\n"
    envError=1
fi

# Check that the GitHub CLI is installed.
if ! command -v gh &> /dev/null
then
    printf "Fatal: gh (the GitHub CLI) is not installed. You can install it running \`brew install gh\`\n"
    envError=1
else
    # Check if gh is authenticated
    if ! gh auth status &> /dev/null; then
        printf "GitHub CLI is not authenticated. Launching authentication...\n"
        gh auth login
        if [ $? -ne 0 ]; then
            printf "Fatal: GitHub CLI authentication failed.\n"
            envError=1
        fi
    fi
fi

# TCHAP - Use Yubikey instead of local keystore
# PKCS11 configuration for YubiKey
pkcs11Config="${TCHAP_X_PKCS11_CONFIG}"
if [[ -z "${pkcs11Config}" ]]; then
    printf "Fatal: TCHAP_X_PKCS11_CONFIG is not defined in the environment.\n\n"
    printf "To fix this:\n"
    printf " 1. Create a PKCS11 config file (e.g., ~/.yubikey/yubikey_tchap.cfg) with this content:\n"
    printf "    name = TchapYubiKey\n"
    printf "    library = /opt/homebrew/lib/libykcs11.dylib (on macOS with Homebrew)\n"
    printf " 2. Add it to your shell profile (~/.zshrc, ~/.bashrc or ~/.profile):\n"
    printf "    export TCHAP_X_PKCS11_CONFIG=\"\$HOME/.yubikey/yubikey_tchap.cfg\"\n"
    printf " 3. Restart your terminal or run 'source ~/.zshrc'.\n\n"
    envError=1
else
    printf "Using PKCS11 config at %s\n" "${pkcs11Config}"
    # Extract library path to check its existence
    libPath=$(grep "library =" "${pkcs11Config}" | cut -d '=' -f 2 | xargs)
    if [[ ! -f "${libPath}" ]]; then
        printf "Fatal: PKCS11 library not found at %s. Check your config file.\n" "${libPath}"
        envError=1
    fi
fi

# Android home
androidHome="${ANDROID_HOME}"
if [[ -z "${androidHome}" ]]; then
    printf "Fatal: ANDROID_HOME is not defined in the environment.\n"
    envError=1
fi

if [ ${envError} == 1 ]; then
  exit 1
fi

# PIN of YubiKey
read -r -s -p "PIN of YubiKey : " yubikeyPin
printf "\n"
if [[ -z "${yubikeyPin}" ]]; then
    printf "Fatal: yubikeyPin is not defined.\n"
    exit 1
fi

# TCHAP - PIN of PIV in YubiKey
keyStorePassword="${yubikeyPin}"
# TCHAP - Alias of the key in YubiKey
keyAlias="X.509 Certificate for PIV Authentication"

# Test connection to YubiKey
function check_yubikey() {
    local pin=$1
    while true; do
        printf "Checking YubiKey connection...\n"
        yubico-piv-tool -a verify-pin -P "${pin}" > /dev/null 2>&1
        if [[ $? -eq 0 ]]; then
            printf "YubiKey connection OK.\n"
            return 0
        fi

        printf "\n/!\\ Fatal: Could not connect to YubiKey.\n"
        printf "Please try to:\n"
        printf " 1. Unplug and replug your YubiKey.\n"
        printf " 2. Wait a few seconds.\n"
        printf " 3. Check if the PIN is locked (run 'ykman piv info').\n"
        read -r -p "Press Enter to try again, or Ctrl+C to abort..."
    done
}

# Fetch notes directly via GH API to format them for Tchap
function get_release_notes() {
  local version=$1

  printf "Fetching release notes from GitHub API...\n"
  local rawNotes
  rawNotes=$(gh api -X POST repos/tchapgouv/tchap-x-android/releases/generate-notes -f tag_name="v${version}" -q .body 2>/dev/null)

  if [[ -z "${rawNotes}" ]]; then
      printf "Warning: Could not fetch release notes automatically.\n"
      releaseNotesContent="* [Changelog complet sur GitHub](https://github.com/tchapgouv/tchap-x-android/releases/tag/v${version})"
      return
  fi

  # TCHAP - Clean release notes: extract only what's after "## What's Changed" if it exists
  local notes="$rawNotes"
  if echo "$notes" | grep -qi "## [Ww]hat.s [Cc]hanged"; then
      notes=$(echo "$notes" | sed -n '/## [Ww]hat.s [Cc]hanged/,$p' | sed '1d')
  fi

  # Always remove leading empty lines from the beginning
  notes=$(echo "$notes" | sed '/./,$!d')

  releaseNotesContent="${notes}"
}

check_yubikey "${yubikeyPin}"

# Handle the result of a version check. ${checkError} must be set to 1 when a check has failed.
checkVersionResult() {
  if [[ ${checkError} -ne 0 ]]; then
    printf "\nThe check above has failed, this is not expected.\n"
    read -r -p "Do you want to continue anyway (yes/no) default to no? " doContinue
    doContinue=${doContinue:-no}
    if [ "${doContinue}" != "yes" ]; then
      exit 1
    fi
  else
    printf "The versions are correct.\n"
  fi
}

# Read minSdkVersion from file plugins/src/main/kotlin/Versions.kt
minSdkVersion=$(grep "MIN_SDK_FOSS =" ./plugins/src/main/kotlin/Versions.kt |cut -d '=' -f 2 |xargs)
# Read buildToolsVersion from file plugins/src/main/kotlin/Versions.kt
buildToolsVersion=$(grep "BUILD_TOOLS_VERSION =" ./plugins/src/main/kotlin/Versions.kt |cut -d '=' -f 2 |xargs)
buildToolsPath="${androidHome}/build-tools/${buildToolsVersion}"

if [[ ! -d ${buildToolsPath} ]]; then
    printf "Fatal: %s folder not found, ensure that you have installed the SDK version %s.\n" "${buildToolsPath}" "${buildToolsVersion}"
    exit 1
fi

# Check that there is no unmerged PR with the label "Z-NextRelease", else exit
unmergedPrs=$(gh pr list --repo tchapgouv/tchap-x-android --label "Z-NextRelease" --state open --json title,url -q '.[] | "\(.url): \(.title)"')
if [[ ${unmergedPrs} != "" ]]; then
    printf "Fatal: There are unmerged PRs with the label Z-NextRelease:\n%s" "${unmergedPrs}"
    printf "\n"
    exit 1
fi


# Check if git flow is enabled
gitFlowDevelop=$(git config gitflow.branch.develop)
if [[ ${gitFlowDevelop} != "" ]]
then
    printf "Git flow is initialized\n"
else
    printf "Git flow is not initialized. Initializing...\n"
    ./tools/gitflow/gitflow-init.sh
fi

printf "OK\n"

printf "\n================================================================================\n"
printf "Ensuring main and develop branches are up to date...\n"

git checkout main
git pull
git checkout develop
git pull

printf "\n================================================================================\n"
# Guessing version to propose a default version
versionsFile="./plugins/src/main/kotlin/Versions.kt"
# The version of the release must match the date of next monday, where the release is supposed to go live
# The command below gets the date of next monday
nextMondayDateCommand="date -v +1w -v -monday"
# Get release year on 2 digits
versionYearCandidate=$(${nextMondayDateCommand} +%y)
currentVersionMonth=$(grep "val versionMonth" ${versionsFile} | cut  -d " " -f6)
# Get release month on 2 digits
versionMonthCandidate=$(${nextMondayDateCommand} +%m)
versionMonthCandidateNoLeadingZero=${versionMonthCandidate/#0/}
currentVersionReleaseNumber=$(grep "val versionReleaseNumber" ${versionsFile} | cut  -d " " -f6)
# if the release month is the same as the current version, we increment the release number, else we reset it to 0
if [[ ${currentVersionMonth} -eq ${versionMonthCandidateNoLeadingZero} ]]; then
  versionReleaseNumberCandidate=$((currentVersionReleaseNumber + 1))
else
  versionReleaseNumberCandidate=0
fi
versionCandidate="${versionYearCandidate}.${versionMonthCandidate}.${versionReleaseNumberCandidate}"

read -r -p "Please enter the release version (example: ${versionCandidate}). Format must be 'YY.MM.x' or 'YY.MM.xy', with year and month matching next Monday. Just press enter if ${versionCandidate} is correct. " version
version=${version:-${versionCandidate}}

# extract year, month and release number for future use
versionYear=$(echo "${version}" | cut  -d "." -f1)
versionMonth=$(echo "${version}" | cut  -d "." -f2)
versionMonthNoLeadingZero=${versionMonth/#0/}
versionReleaseNumber=$(echo "${version}" | cut  -d "." -f3)

printf -v versionReleaseNumber2Digits "%02d" "${versionReleaseNumber}"
versionCode="20${versionYear}${versionMonth}${versionReleaseNumber2Digits}0"

# Check if the tag already exists (Tchap Resume Logic)
tag_exists=$(git tag -l "v${version}")
if [[ -n "${tag_exists}" ]]; then
    printf "Tag v%s already exists. Skipping release creation and resuming from artifact download...\n" "${version}"
    is_resuming=1
else
    is_resuming=0
fi

if [[ ${is_resuming} == 1 ]]; then
    printf "\n================================================================================\n"
    printf "Mmh, it seems that the release is already started and tag for version already pushed.\n"
    printf "Do you want to continue the release using the created tag?\n\n"
    read -r -p "Continue (yes/no) default to yes? " doContinue
    doContinue=${doContinue:-yes}
    if [ "${doContinue}" == "no" ]; then
      read -r -p "Do you want to delete the local tag? (yes/no) default to no: " doDeleteTag
      doDeleteTag=${doDeleteTag:-no}
      if [ "${doDeleteTag}" == "yes" ]; then
        printf "Deleting local tag v%s...\n" "${version}"
        git tag -d "v${version}"
        is_resuming=0
      else
        printf "OK, exiting\n"
        exit 1
      fi
    fi
fi

if [[ ${is_resuming} == 0 ]]; then
    printf "\n================================================================================\n"
    printf "Starting the release %s\n" "${version}"
    git flow release start "${version}"

    # Note: in case the release is already started and the script is started again, checkout the release branch again.
    ret=$?
    if [[ $ret -ne 0 ]]; then
      printf "Mmh, it seems that the release is already started. I'm displaying the changes now:\n"
      git diff --stat "release/${version}" origin/main
      printf "Do you want to continue the release using its contents?\n\n"
      read -r -p "Continue (yes/no) default to yes? " doContinue
      doContinue=${doContinue:-yes}
      if [ "${doContinue}" == "no" ]; then
        printf "OK, exiting, you can start the release again with the command 'git flow release start %s'\n" "${version}"
        exit 1
      fi
      git checkout "release/${version}"
    fi

    # Ensure version is OK
    versionsFileBak="${versionsFile}.bak"
    cp ${versionsFile} ${versionsFileBak}
    sed "s/private const val versionYear = .*/private const val versionYear = ${versionYear}/" ${versionsFileBak} > ${versionsFile}
    sed "s/private const val versionMonth = .*/private const val versionMonth = ${versionMonthNoLeadingZero}/" ${versionsFile}    > ${versionsFileBak}
    sed "s/private const val versionReleaseNumber = .*/private const val versionReleaseNumber = ${versionReleaseNumber}/" ${versionsFileBak} > ${versionsFile}
    rm ${versionsFileBak}

    # Update the file aaptDump.txt with the new version
    aaptDumpFile="./tools/manifest/gplay/release/aaptDump.txt"
    sed "s/versionCode='[0-9]*'/versionCode='${versionCode}'/" ${aaptDumpFile} > ${aaptDumpFile}.bak
    sed "s/versionName='[0-9]*\.[0-9]*\.[0-9]*'/versionName='${version}'/" ${aaptDumpFile}.bak > ${aaptDumpFile}
    rm ${aaptDumpFile}.bak

    git commit -a -m "Version ${version}"

    printf "\n================================================================================\n"
    printf "OK, finishing the release...\n"
    # GIT_MERGE_AUTOEDIT avoids opening the editor for the 2 merge commits, whose default message is
    # always used, and -m provides the message of the annotated tag. git flow appends the tag name to
    # it, so the tag message ends up being "Release v${version}".
    GIT_MERGE_AUTOEDIT=no git flow release finish -m "v${version}" "${version}"

    printf "\n================================================================================\n"
    read -r -p "Done, push the branch 'main' and the new tag (yes/no) default to yes? " doPush
    doPush=${doPush:-yes}

    if [ "${doPush}" == "yes" ]; then
      printf "Pushing branch 'main' and tag 'v%s'...\n" "${version}"
      git push origin main
      git push origin "v${version}"
    else
        printf "Not pushing, do not forget to push manually!\n"
    fi
fi

printf "\n================================================================================\n"
printf "Checking out develop...\n"
git checkout develop

printf "\n================================================================================\n"
printf "Downloading the artifacts...\n"

targetPath="./tmp/Tchap/${version}"
fdroidTargetPath="${targetPath}/fdroid"
gplayTargetPath="${targetPath}/gplay"

releaseCommit=$(git rev-parse --verify --quiet "v${version}^{commit}")
if [[ -z "${releaseCommit}" ]]; then
  # Without a commit, `gh run list` would return the latest run of the workflow, which may be another one.
  printf "Fatal: the tag v%s cannot be resolved.\n" "${version}"
  exit 1
fi

printf "Looking for the run of the workflow release.yml for the commit %s...\n" "${releaseCommit}"

runId=""

# The run can take a few seconds to appear after the push, so retry for a couple of minutes.
for _ in $(seq 1 12); do
  runId=$(gh run list --repo tchapgouv/tchap-x-android --workflow release.yml --commit "${releaseCommit}" --limit 1 --json databaseId -q '.[0].databaseId' 2> /dev/null)
  if [[ -n "${runId}" ]]; then
    break
  fi
  printf "No run found yet, waiting...\n"
  sleep 10
done

if [[ -z "${runId}" ]]; then
  printf "Fatal: no run of the workflow release.yml found for the commit %s.\n" "${releaseCommit}"
  exit 1
fi

printf "Found the run https://github.com/tchapgouv/tchap-x-android/actions/runs/%s\n" "${runId}"
printf "Waiting for the run to complete...\n"
gh run watch "${runId}" --repo tchapgouv/tchap-x-android --compact --exit-status

ret=1

while [[ $ret -ne 0 ]]; do
  gh run download "${runId}" --repo tchapgouv/tchap-x-android \
     --dir "${gplayTargetPath}" \
     --name app-gplay-tchap-withpinning-bundle-unsigned

  ret=$?
  if [[ $ret -eq 0 ]]; then
    gh run download "${runId}" --repo tchapgouv/tchap-x-android \
       --dir "${fdroidTargetPath}" \
       --name app-fdroid-tchap-withoutpinning-apks-unsigned

    ret=$?
  fi
  if [[ $ret -ne 0 ]]; then
    read -r -p "Error while downloading the artifacts. You may want to fix the issue and retry. Retry (yes/no) default to yes? " doRetry
    doRetry=${doRetry:-yes}
    if [ "${doRetry}" == "no" ]; then
      exit 1
    fi
  fi
done

printf "\n================================================================================\n"
printf "Signing the FDroid APKs...\n"

# TCHAP - Final check of YubiKey before starting the signature process
check_yubikey "${yubikeyPin}"

cp "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release.apk \
   "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release-signed.apk
"${buildToolsPath}"/apksigner -J-add-exports=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED sign \
       -v \
       --alignment-preserved true \
       --ks NONE \
       --ks-type PKCS11 \
       --provider-class sun.security.pkcs11.SunPKCS11 \
       --provider-arg "${pkcs11Config}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias "${keyAlias}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release-signed.apk

cp "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-armeabi-v7a-release.apk \
   "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-armeabi-v7a-release-signed.apk
"${buildToolsPath}"/apksigner -J-add-exports=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED sign \
       -v \
       --alignment-preserved true \
       --ks NONE \
       --ks-type PKCS11 \
       --provider-class sun.security.pkcs11.SunPKCS11 \
       --provider-arg "${pkcs11Config}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias "${keyAlias}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-armeabi-v7a-release-signed.apk

cp "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86-release.apk \
   "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86-release-signed.apk
"${buildToolsPath}"/apksigner -J-add-exports=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED sign \
       -v \
       --alignment-preserved true \
       --ks NONE \
       --ks-type PKCS11 \
       --provider-class sun.security.pkcs11.SunPKCS11 \
       --provider-arg "${pkcs11Config}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias "${keyAlias}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86-release-signed.apk

cp "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86_64-release.apk \
   "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86_64-release-signed.apk
"${buildToolsPath}"/apksigner -J-add-exports=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED sign \
       -v \
       --alignment-preserved true \
       --ks NONE \
       --ks-type PKCS11 \
       --provider-class sun.security.pkcs11.SunPKCS11 \
       --provider-arg "${pkcs11Config}" \
       --ks-pass pass:"${keyStorePassword}" \
       --ks-key-alias "${keyAlias}" \
       --min-sdk-version "${minSdkVersion}" \
       "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86_64-release-signed.apk

printf "\n================================================================================\n"
printf "Checking the signed APKs...\n"

checkError=0

# Each APK gets the version code of the app bundle, plus the code of its ABI.
# Must be kept in sync with the abiVersionCodes map in app/build.gradle.kts.
fdroidAbis="armeabi-v7a:1 arm64-v8a:2 x86:3 x86_64:4"

for abiEntry in ${fdroidAbis}; do
  abi="${abiEntry%%:*}"
  abiCode="${abiEntry##*:}"
  expectedApkVersionCode=$((versionCode + abiCode))
  apkFile="${fdroidTargetPath}/app-fdroid-tchap-withoutpinning-${abi}-release-signed.apk"
  apkBadging=$("${buildToolsPath}"/aapt dump badging "${apkFile}" | grep -m 1 "^package")
  apkVersionCode=$(printf "%s" "${apkBadging}" | sed -n "s/.*versionCode='\([0-9]*\)'.*/\1/p")
  apkVersionName=$(printf "%s" "${apkBadging}" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p")
  printf "File app-fdroid-tchap-withoutpinning-%s-release-signed.apk: version code %s, version name %s\n" "${abi}" "${apkVersionCode}" "${apkVersionName}"
  if [[ "${apkVersionCode}" != "${expectedApkVersionCode}" ]]; then
    printf "Warning: was expecting the version code %s, but got %s.\n" "${expectedApkVersionCode}" "${apkVersionCode}"
    checkError=1
  fi
  if [[ "${apkVersionName}" != "${version}" ]]; then
    printf "Warning: was expecting the version name %s, but got %s.\n" "${version}" "${apkVersionName}"
    checkError=1
  fi
done

checkVersionResult

printf "\n================================================================================\n"
printf "The APKs in %s have been signed!\n" "${fdroidTargetPath}"

unsignedBundlePath="${gplayTargetPath}/app-gplay-tchap-withpinning-release.aab"
signedBundlePath="${gplayTargetPath}/app-gplay-tchap-withpinning-release-signed.aab"

printf "\n================================================================================\n"
printf "Signing file %s with build-tools version %s for min SDK version %s...\n" "${unsignedBundlePath}" "${buildToolsVersion}" "${minSdkVersion}"

cp "${unsignedBundlePath}" "${signedBundlePath}"

"${buildToolsPath}"/apksigner -J-add-exports=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED sign \
    -v \
    --alignment-preserved true \
    --ks NONE \
    --ks-type PKCS11 \
    --provider-class sun.security.pkcs11.SunPKCS11 \
    --provider-arg "${pkcs11Config}" \
    --ks-pass pass:"${keyStorePassword}" \
    --ks-key-alias "${keyAlias}" \
    --min-sdk-version "${minSdkVersion}" \
    "${signedBundlePath}"

printf "\n================================================================================\n"
printf "Checking the signed app bundle...\n"

checkError=0

bundleVersionCode=$(bundletool dump manifest --bundle="${signedBundlePath}" --xpath=/manifest/@android:versionCode)
bundleVersionName=$(bundletool dump manifest --bundle="${signedBundlePath}" --xpath=/manifest/@android:versionName)
printf "File %s: version code %s, version name %s\n" "$(basename "${signedBundlePath}")" "${bundleVersionCode}" "${bundleVersionName}"
if [[ "${bundleVersionCode}" != "${versionCode}" ]]; then
  printf "Warning: was expecting the version code %s, but got %s.\n" "${versionCode}" "${bundleVersionCode}"
  checkError=1
fi
if [[ "${bundleVersionName}" != "${version}" ]]; then
  printf "Warning: was expecting the version name %s, but got %s.\n" "${version}" "${bundleVersionName}"
  checkError=1
fi

checkVersionResult

printf "\n================================================================================\n"
printf "The file %s has been signed and can be uploaded to the PlayStore!\n" "${signedBundlePath}"

printf "\n================================================================================\n"
printf "Collecting all signed artifacts into a single folder...\n"
signedReleaseDir="${targetPath}/SIGNED_RELEASE"
mkdir -p "${signedReleaseDir}"
cp "${fdroidTargetPath}"/*-signed.apk "${signedReleaseDir}/"
cp "${signedBundlePath}" "${signedReleaseDir}/"

printf "All signed artifacts are available in: %s\n" "${signedReleaseDir}"

if [[ "$OSTYPE" == "darwin"* ]]; then
  read -r -p "Do you want to open this folder in Finder (yes/no) default to yes? " doOpenFinder
  doOpenFinder=${doOpenFinder:-yes}
  if [ "${doOpenFinder}" == "yes" ]; then
    open "${signedReleaseDir}"
  fi
fi

printf "\n================================================================================\n"
read -r -p "Do you want to build the APKs from the app bundle? You need to do this step if you want to install the application to your device. (yes/no) default to no " doBuildApks
doBuildApks=${doBuildApks:-no}

if [ "${doBuildApks}" == "yes" ]; then
  printf "Building apks...\n"
  bundletool build-apks --bundle="${signedBundlePath}" --output="${gplayTargetPath}"/tchapx.apks \
      --ks=./app/signature/debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --key-pass=pass:android \
      --overwrite

  read -r -p "Do you want to install the application to your device? Make sure there is one (and only one!) connected device first. (yes/no) default to yes " doDeploy
  doDeploy=${doDeploy:-yes}
  if [ "${doDeploy}" == "yes" ]; then
    printf "Installing apk for your device...\n"
    bundletool install-apks --apks="${gplayTargetPath}"/tchapx.apks
    read -r -p "Please run the application on your phone to check that the upgrade went well. Press enter to continue. "
  else
    printf "APK will not be deployed!\n"
  fi
else
  printf "APKs will not be generated!\n"
fi

printf "\n================================================================================\n"
printf "Generate Release Notes and Google Play payload...\n"

get_release_notes "${version}"

# TCHAP - Generate Google Play specific notes
googlePlayNotes=$(echo "$releaseNotesContent" | grep "^* " | sed 's/^* /- /' | sed 's/ by @.*//')
printf "Version name :\n${version}\n\n<fr-FR>\nCette version de Tchap apporte les nouveautés suivantes :\n\n${googlePlayNotes}\n</fr-FR>\n\n"

printf "\n================================================================================\n"
printf "Create the open testing release on GooglePlay.\n"

printf "Go to GooglePlay console : https://play.google.com/console/u/0/developers/7057431657963963746/app/4975852839646682683/tracks/internal-testing\n"
printf "On GooglePlay console, go to the internal testing section and click on \"Create new release\" button, then:\n"
printf " - upload the file %s.\n" "${signedBundlePath}"
printf " - copy the VersionName & ReleaseNotes displayed above.\n"
printf " - download the universal APK, to be able to provide it to the GitHub release: click on the right arrow next to the \"App bundle\", then click on the \"Download\" tab, and download the \"Signed, universal APK\".\n"
read -r -p "Press enter to continue. "

printf "Click \"next\" to go to \"Publishing overview\" and send the new release for a review by Google.\n"
read -r -p "Press enter to continue. "

# TCHAP - Try to move and rename the downloaded universal signed APK
baseVersionCode=$(((10#2000 + ${versionYear}) * 10000 + 10#${versionMonthNoLeadingZero} * 100 + 10#${versionReleaseNumber}))
universalVersionCode=$((baseVersionCode * 10))
universalApkName="${universalVersionCode}.apk"

# Check in ~/Downloads and ~/Download
downloadedApkPath=""
if [[ -f "${HOME}/Downloads/${universalApkName}" ]]; then
    downloadedApkPath="${HOME}/Downloads/${universalApkName}"
elif [[ -f "${HOME}/Download/${universalApkName}" ]]; then
    downloadedApkPath="${HOME}/Download/${universalApkName}"
fi

if [[ -n "${downloadedApkPath}" ]]; then
    printf "Found universal APK at %s. Moving to %s...\n" "${downloadedApkPath}" "${signedReleaseDir}"
    mv "${downloadedApkPath}" "${signedReleaseDir}/app-gplay-tchap-withpinning-universal-release-signed.apk"
else
    printf "Warning: Universal APK %s not found in ~/Downloads or ~/Download. You will need to move it to %s manually if you want it included.\n" "${universalApkName}" "${signedReleaseDir}"
    read -r -p "Press enter when ready to continue. "
fi

universalApkPath="${signedReleaseDir}/app-gplay-tchap-withpinning-universal-release-signed.apk"

printf "\n================================================================================\n"
printf "Creating the release on GitHub.\n"

releaseAssets=(
  "${signedBundlePath}"
  "${universalApkPath}"
  "${signedReleaseDir}/app-fdroid-tchap-withoutpinning-arm64-v8a-release-signed.apk"
  "${signedReleaseDir}/app-fdroid-tchap-withoutpinning-armeabi-v7a-release-signed.apk"
  "${signedReleaseDir}/app-fdroid-tchap-withoutpinning-x86-release-signed.apk"
  "${signedReleaseDir}/app-fdroid-tchap-withoutpinning-x86_64-release-signed.apk"
)

missingAsset=0

for releaseAsset in "${releaseAssets[@]}"; do
  if [[ ! -f "${releaseAsset}" ]]; then
    printf "Error: the file %s does not exist.\n" "${releaseAsset}"
    missingAsset=1
  fi
done

if [[ ${missingAsset} -ne 0 ]]; then
  printf "Fatal: some files are missing, cannot create the GitHub release.\n"
  exit 1
fi

printf "Creating the release v%s and uploading the %d files, this can take a while...\n" "${version}" "${#releaseAssets[@]}"

gh release create "v${version}" \
   --repo tchapgouv/tchap-x-android \
   --title "${appName} v${version}" \
   --notes "${releaseNotesContent}" \
   --verify-tag \
   "${releaseAssets[@]}"

if [[ $? -ne 0 ]]; then
  printf "Fatal: error while creating the GitHub release.\n"
  exit 1
fi

printf "The release has been created: https://github.com/tchapgouv/tchap-x-android/releases/tag/v%s\n" "${version}"
printf "Please verify that all files are correctly uploaded.\n"
read -r -p "Press enter to continue. "

printf "\n================================================================================\n"
printf "Update the project release notes:\n\n"

tchapChangesFile="CHANGES_TCHAP.md"
tchapChangesFileBak="${tchapChangesFile}.tmp"
changesTitle="Changements dans ${appName} v${version}"
changesUnderline="${changesTitle//?/=}"

printf "Updating %s...\n" "${tchapChangesFile}"

printf "%s\n%s\n\n<!-- Release notes generated using configuration in .github/release.yml at v${version} -->\n\n## Qu'est-ce qui a changé ?\n%s\n\n" "${changesTitle}" "${changesUnderline}" "${releaseNotesContent}" > "${tchapChangesFileBak}"
cat "${tchapChangesFile}" >> "${tchapChangesFileBak}"
mv "${tchapChangesFileBak}" "${tchapChangesFile}"

printf "The file %s has been updated.\n" "${tchapChangesFile}"
read -r -p "Please check the change and press enter to commit it. "

printf "\n================================================================================\n"
printf "Committing...\n"
git commit -a -m "${changesTitle}"

printf "\n================================================================================\n"
read -r -p "Done, push the branch 'develop' (yes/no) default to yes? (A rebase may be necessary in case develop got new commits) " doPush
doPush=${doPush:-yes}

if [ "${doPush}" == "yes" ]; then
  printf "Pushing branch 'develop'...\n"
  git push origin develop
else
    printf "Not pushing, do not forget to push manually!\n"
fi

printf "\n================================================================================\n"
printf "Message for the Android internal room:\n\n"
printf "# ${appName} v${version}\n\n## Qu'est-ce qui a changé ?\n${releaseNotesContent}\n\n[🧑‍💻 Voir la version sur GitHub](https://github.com/tchapgouv/tchap-x-android/releases/tag/v${version})\n[🚀 Forcer la mise à jour manuelle via le Play Store](https://play.google.com/store/apps/details?id=fr.gouv.tchap.android.x)\n\n"
read -r -p "Send the message manually, and press enter to continue. "

printf "\n================================================================================\n"
printf "Generate SHA256 of all signed APKs :\n\n"

printf "Bonjour,\nLa dernière version de ${appName} v${version} est disponible.\n\nVoici la liste des hash SHA256 :\n\n\`\`\`\n"
# Generate SHA256 of all apk in signedReleaseDir
  (
    cd "${signedReleaseDir}" || exit
    for f in *.apk; do
      if [[ "$OSTYPE" == "darwin"* ]]; then
        shasum -a 256 "$f"
      else
        sha256sum "$f"
      fi
    done
  )
printf "\`\`\`\n\n[🧑‍💻 Voir la version sur GitHub](https://github.com/tchapgouv/tchap-x-android/releases/tag/v${version})\n\n"

read -r -p "Send the message manually in required rooms (see the list of required rooms here: https://docs.numerique.gouv.fr/docs/58817f90-248f-45d1-bf00-661fe933058d/). Then, press enter to continue. "

printf "\n================================================================================\n"
read -r -p "Would you like to remove the temporary directory containing the downloaded and signed artifacts (yes/no) default to yes? " doCleanArtifacts
doCleanArtifacts=${doCleanArtifacts:-yes}

if [ "${doCleanArtifacts}" == "yes" ]; then
  printf "Cleaning up temporary directory...\n"
  rm -rf "${targetPath}"
else
    printf "Temporary directory preserved. Don't forget to clean it up later: %s\n" "${targetPath}"
fi

printf "\n================================================================================\n"
printf "Logging out from GitHub CLI...\n"
gh auth logout

printf "\n================================================================================\n"
printf "Congratulation! Kudos for using this script! Have a nice day!\n"
printf "================================================================================\n"
