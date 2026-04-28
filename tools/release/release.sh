#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# do not exit when any command fails (issue with git flow)
set +e

appName="Tchap beta Android"

printf "\n================================================================================\n"
printf "|                    Welcome to the release script!                            |\n"
printf "================================================================================\n"

printf "Checking environment...\n"
envError=0

# Check that bundletool is installed
if ! command -v bundletool &> /dev/null
then
    printf "Fatal: bundletool is not installed. You can install it running \`brew install bundletool\`\n"
    envError=1
fi

# Check Python dependencies
printf "Checking Python dependencies...\n"
if ! python3 -c "import requests" &> /dev/null; then
    printf "Python 'requests' module is missing. Attempting to install...\n"
    python3 -m pip install requests --break-system-packages || {
        printf "Fatal: Could not install Python 'requests' module. Please run 'pip3 install requests'\n"
        envError=1
    }
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

# PIN of YubiKey
read -r -s -p "PIN of YubiKey : " yubikeyPin
printf "\n"
if [[ -z "${yubikeyPin}" ]]; then
    printf "Fatal: yubikeyPin is not defined.\n"
    envError=1
fi

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

if [[ ${envError} == 0 ]]; then
    check_yubikey "${yubikeyPin}"
fi

# TCHAP - PIN of PIV in YubiKey
keyStorePassword="${yubikeyPin}"
# TCHAP - Alias of the key in YubiKey
keyAlias="X.509 Certificate for PIV Authentication"

# GitHub token
gitHubToken="${TCHAP_GITHUB_TOKEN}"
if [[ -z "${gitHubToken}" ]]; then
    printf "Fatal: TCHAP_GITHUB_TOKEN is not defined in the environment.\n\n"
    printf "To fix this:\n"
    printf " 1. Go to https://github.com/settings/tokens\n"
    printf " 2. Click 'Generate new token (classic)'\n"
    printf " 3. Give it a name (e.g., 'Tchap Release Script') and select the 'repo' & 'workflow' scopes.\n"
    printf " 4. Copy the token and add it to your shell profile (~/.zshrc, ~/.bashrc or ~/.profile):\n"
    printf "    export TCHAP_GITHUB_TOKEN=\"your_token_here\"\n"
    printf " 5. Restart your terminal or run 'source ~/.zshrc' (or your shell profile file).\n\n"
    envError=1
fi
# Android home
androidHome="${ANDROID_HOME}"
if [[ -z "${androidHome}" ]]; then
    printf "Fatal: ANDROID_HOME is not defined in the environment.\n"
    envError=1
fi

# TCHAP - Disable matrix Bot token
## @elementbot:matrix.org matrix token / Not mandatory
#elementBotToken="${ELEMENT_BOT_MATRIX_TOKEN}"
#if [[ -z "${elementBotToken}" ]]; then
#    printf "Warning: ELEMENT_BOT_MATRIX_TOKEN is not defined in the environment.\n"
#fi

if [ ${envError} == 1 ]; then
  exit 1
fi

# Read minSdkVersion from file plugins/src/main/kotlin/Versions.kt
minSdkVersion=$(grep "MIN_SDK_FOSS =" ./plugins/src/main/kotlin/Versions.kt |cut -d '=' -f 2 |xargs)
# Read buildToolsVersion from file plugins/src/main/kotlin/Versions.kt
buildToolsVersion=$(grep "BUILD_TOOLS_VERSION =" ./plugins/src/main/kotlin/Versions.kt |cut -d '=' -f 2 |xargs)
buildToolsPath="${androidHome}/build-tools/${buildToolsVersion}"

if [[ ! -d ${buildToolsPath} ]]; then
    printf "Fatal: %s folder not found, ensure that you have installed the SDK version %s.\n" "${buildToolsPath}" "${buildToolsVersion}"
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
# Reading version from file
versionsFile="./plugins/src/main/kotlin/Versions.kt"

# TCHAP - Use major/minor/patch format instead of year/month
# Guessing version to propose a default version
# The version of the release must match the date of next monday, where the release is supposed to go live
# The command below gets the date of next monday
# nextMondayDateCommand="date -v +1w -v -monday"
# Get release year on 2 digits
# versionYearCandidate=$(${nextMondayDateCommand} +%y)
# currentVersionMonth=$(grep "val versionMonth" ${versionsFile} | cut  -d " " -f6)
# Get release month on 2 digits
# versionMonthCandidate=$(${nextMondayDateCommand} +%m)
# versionMonthCandidateNoLeadingZero=${versionMonthCandidate/#0/}
# currentVersionReleaseNumber=$(grep "val versionReleaseNumber" ${versionsFile} | cut  -d " " -f6)
# if the release month is the same as the current version, we increment the release number, else we reset it to 0
# if [[ ${currentVersionMonth} -eq ${versionMonthCandidateNoLeadingZero} ]]; then
#  versionReleaseNumberCandidate=$((currentVersionReleaseNumber + 1))
# else
#  versionReleaseNumberCandidate=0
# fi
# versionCandidate="${versionYearCandidate}.${versionMonthCandidate}.${versionReleaseNumberCandidate}"

versionMajorNumber=$(grep "private const val versionMajorNumber =" ${versionsFile} | cut -d '=' -f 2 | xargs)
versionMinorNumber=$(grep "private const val versionMinorNumber =" ${versionsFile} | cut -d '=' -f 2 | xargs)
versionPatchNumber=$(grep "private const val versionPatchNumber =" ${versionsFile} | cut -d '=' -f 2 | xargs)
versionCandidate="${versionMajorNumber}.${versionMinorNumber}.${versionPatchNumber}"

read -r -p "Please enter the release version (example: ${versionCandidate}). Just press enter if ${versionCandidate} is correct. " version
version=${version:-${versionCandidate}}

# extract major, minor and patch for future use
versionMajorNumber=$(echo "${version}" | cut  -d "." -f1)
versionMinorNumber=$(echo "${version}" | cut  -d "." -f2)
versionMinorNumberNoLeadingZero=${versionMinorNumber/#0/}
versionPatchNumber=$(echo "${version}" | cut  -d "." -f3)

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
# versionsFileBak="${versionsFile}.bak"
# cp ${versionsFile} ${versionsFileBak}
# sed "s/private const val versionYear = .*/private const val versionYear = ${versionYear}/" ${versionsFileBak} > ${versionsFile}
# sed "s/private const val versionMonth = .*/private const val versionMonth = ${versionMonthNoLeadingZero}/" ${versionsFile}    > ${versionsFileBak}
# sed "s/private const val versionReleaseNumber = .*/private const val versionReleaseNumber = ${versionReleaseNumber}/" ${versionsFileBak} > ${versionsFile}
# rm ${versionsFileBak}

versionsFileBak="${versionsFile}.bak"
cp ${versionsFile} ${versionsFileBak}
sed "s/private const val versionMajorNumber = .*/private const val versionMajorNumber = ${versionMajorNumber}/" ${versionsFileBak} > ${versionsFile}
sed "s/private const val versionMinorNumber = .*/private const val versionMinorNumber = ${versionMinorNumberNoLeadingZero}/" ${versionsFile}    > ${versionsFileBak}
sed "s/private const val versionPatchNumber = .*/private const val versionPatchNumber = ${versionPatchNumber}/" ${versionsFileBak} > ${versionsFile}
rm ${versionsFileBak}

printf "\n================================================================================\n"
appNameURI=$(echo "$appName" | sed "s/ /%20/g")
githubCreateReleaseLink="https://github.com/tchapgouv/tchap-x-android/releases/new?tag=v${version}&title=${appNameURI}%20v${version}"
printf "Generating release notes...\n"
printf -- "Open this link: %s\n" "${githubCreateReleaseLink}"
printf "Then:\n"
printf " - Click on the 'Generate release notes' button on GitHub.\n"
printf " - Optionally reorder items and fix typos.\n"
printf " - Copy the generated notes.\n"

tchapChangesFile="CHANGES_TCHAP.md"
if [[ "$OSTYPE" == "darwin"* ]]; then
  read -r -p "Press Enter once you have copied the notes to your clipboard (I'll read them from there)... "
  releaseNotesContent=$(pbpaste)
else
  printf "Paste notes below and press Ctrl+D once finished:\n"
  releaseNotesContent=$(cat)
fi

# TCHAP - Clean release notes: extract only what's after "## What's Changed" if it exists
if echo "$releaseNotesContent" | grep -qi "## [Ww]hat.s [Cc]hanged"; then
    releaseNotesContent=$(echo "$releaseNotesContent" | sed -n '/## [Ww]hat.s [Cc]hanged/,$p' | sed '1d')
fi

# Always remove leading empty lines from the beginning
releaseNotesContent=$(echo "$releaseNotesContent" | sed '/./,$!d')

printf "Changements dans ${appName} v${version}\n=============================\n\n<!-- Release notes generated using configuration in .github/release.yml at v${version} -->\n\n## Qu'est-ce qui a changé ?\n${releaseNotesContent}\n\n" > "${tchapChangesFile}.tmp"
cat "${tchapChangesFile}" >> "${tchapChangesFile}.tmp"
mv "${tchapChangesFile}.tmp" "${tchapChangesFile}"

printf "Committing version...\n"
git commit -a -m "Version ${version}"

# TCHAP - Disable fastlane
#printf "\n================================================================================\n"
#printf "Creating fastlane file...\n"
#printf -v versionPatchNumber2Digits "%02d" "${versionPatchNumber}"
#fastlaneFile="${versionMajorNumber}${versionMinorNumber}${versionPatchNumber2Digits}0.txt"
#fastlanePathFile="./fastlane/metadata/android/en-US/changelogs/${fastlaneFile}"
#printf "Main changes in this version: bug fixes and improvements.\nFull changelog: https://github.com/tchapgouv/tchap-x-android/releases" > "${fastlanePathFile}"
#
#read -r -p "I have created the file ${fastlanePathFile}, please edit it and press enter to continue. "
#git add "${fastlanePathFile}"
#git commit -a -m "Adding fastlane file for version ${version}"

printf "\n================================================================================\n"
printf "OK, finishing the release...\n"
git flow release finish -m "v${version}" "${version}"

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

printf "\n================================================================================\n"
printf "Checking out develop...\n"
git checkout develop

printf "\n================================================================================\n"
printf "The GitHub action https://github.com/tchapgouv/tchap-x-android/actions/workflows/release.yml?query=branch%%3Amain should have start a new run.\n"
read -r -p "Please enter the url of the run, no need to wait for it to complete (example: https://github.com/tchapgouv/tchap-x-android/actions/runs/9065756777): " runUrl

targetPath="./tmp/Tchap/${version}"

printf "\n================================================================================\n"
printf "Downloading the artifacts...\n"

ret=1

while [[ $ret -ne 0 ]]; do
  python3 ./tools/github/download_all_github_artifacts.py \
     --token "${gitHubToken}" \
     --runUrl "${runUrl}" \
     --directory "${targetPath}"

  ret=$?
  if [[ $ret -ne 0 ]]; then
    read -r -p "Error while downloading the artifacts. You may want to fix the issue and retry. Retry (yes/no) default to yes? " doRetry
    doRetry=${doRetry:-yes}
    if [ "${doRetry}" == "no" ]; then
      exit 1
    fi
  fi
done

printf "\n================================================================================\n"
printf "Unzipping the F-Droid artifact...\n"

fdroidTargetPath="${targetPath}/fdroid"
unzip "${targetPath}"/app-fdroid-tchap-withoutpinning-apks-unsigned.zip -d "${fdroidTargetPath}"

printf "\n================================================================================\n"
printf "Patching the FDroid APKs using inplace-fix.py...\n"

inplaceFixScript="./tools/release/inplace-fix.py"
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release.apk   '0000000'
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-armeabi-v7a-release.apk '0000000'
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86-release.apk         '0000000'
python3 "${inplaceFixScript}" --page-size 16 fix-pg-map-id "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86_64-release.apk      '0000000'

printf "\n================================================================================\n"
printf "Signing the FDroid APKs...\n"

# TCHAP - Final check of YubiKey before starting the signature process
check_yubikey "${yubikeyPin}"

cp "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release.apk \
   "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release-signed.apk

# TCHAP - Use Yubikey in apksigner
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

# TCHAP - Use Yubikey in apksigner
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

# TCHAP - Use Yubikey in apksigner
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

# TCHAP - Use Yubikey in apksigner
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
printf "Please check the information below:\n"

printf "File app-fdroid-tchap-withoutpinning-arm64-v8a-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-arm64-v8a-release-signed.apk | grep package
printf "File app-fdroid-tchap-withoutpinning-armeabi-v7a-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-armeabi-v7a-release-signed.apk | grep package
printf "File app-fdroid-tchap-withoutpinning-x86-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86-release-signed.apk | grep package
printf "File app-fdroid-tchap-withoutpinning-x86_64-release-signed.apk:\n"
"${buildToolsPath}"/aapt dump badging "${fdroidTargetPath}"/app-fdroid-tchap-withoutpinning-x86_64-release-signed.apk | grep package

printf "\n"
read -r -p "Does it look correct? Press enter when it's done. "

printf "\n================================================================================\n"
printf "The APKs in %s have been signed!\n" "${fdroidTargetPath}"

printf "\n================================================================================\n"
printf "Unzipping the Gplay artifact...\n"

gplayTargetPath="${targetPath}/gplay"
unzip "${targetPath}"/app-gplay-tchap-withpinning-bundle-unsigned.zip -d "${gplayTargetPath}"

unsignedBundlePath="${gplayTargetPath}/app-gplay-tchap-withpinning-release.aab"
signedBundlePath="${gplayTargetPath}/app-gplay-tchap-withpinning-release-signed.aab"

printf "\n================================================================================\n"
printf "Signing file %s with build-tools version %s for min SDK version %s...\n" "${unsignedBundlePath}" "${buildToolsVersion}" "${minSdkVersion}"

cp "${unsignedBundlePath}" "${signedBundlePath}"

# TCHAP - Use Yubikey in apksigner
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
printf "Please check the information below:\n"

printf "Version code: "
bundletool dump manifest --bundle="${signedBundlePath}" --xpath=/manifest/@android:versionCode
printf "Version name: "
bundletool dump manifest --bundle="${signedBundlePath}" --xpath=/manifest/@android:versionName

printf "\n"
read -r -p "Does it look correct? Press enter to continue. "

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
printf "Create the open testing release on GooglePlay.\n\n"

# TCHAP - Generate Google Play specific notes
# Keep lines starting with *, remove the bullet point and space & remove everything from " by @" to the end of the line
googlePlayNotes=$(echo "$releaseNotesContent" | grep "^* " | sed 's/^* /- /' | sed 's/ by @.*//')
printf "<fr-FR>\nCette version de Tchap apporte les nouveautés suivantes :\n\n${googlePlayNotes}\n</fr-FR>\n\n"

printf "Go to GooglePlay console : https://play.google.com/console/u/0/developers/7057431657963963746/app/4975852839646682683/tracks/open-testing\n"
printf "On GooglePlay console, go the the open testing section and click on \"Create new release\" button, then:\n"
printf " - upload the file %s.\n" "${signedBundlePath}"
printf " - copy the release note displayed above.\n"
printf " - download the universal APK, to be able to provide it to the GitHub release: click on the right arrow next to the \"App bundle\", then click on the \"Download\" tab, and download the \"Signed, universal APK\".\n"
printf " - submit the release.\n"
read -r -p "Press enter to continue. "

printf "You can then go to \"Publishing overview\" and send the new release for a review by Google.\n"
read -r -p "Press enter to continue. "

printf "\n================================================================================\n"
printf "Creating the release on gitHub.\n"
printf -- "Return to (or re-open) this link: %s\n" "${githubCreateReleaseLink}"
printf "Then\n"
printf " - If needed : copy the release note from the %s file.\n" "${tchapChangesFile}"
printf " - Add the file %s to the GitHub release.\n" "${signedBundlePath}"
printf " - Add the universal APK, downloaded from the GooglePlay console to the GitHub release.\n"
printf " - Add the 4 signed APKs for F-Droid, located at %s to the GitHub release.\n" "${fdroidTargetPath}"
read -r -p ". Press enter to continue. "

# TCHAP - Disable "Update release notes" (done before in CHANGES_TCHAP.md)
#printf "\n================================================================================\n"
#printf "Update the project release notes:\n\n"
#
#read -r -p "Copy the content of the release note generated by GitHub to the file CHANGES.md and press enter to commit the change. "
#
#printf "\n================================================================================\n"
#printf "Committing...\n"
#git commit -a -m "Changelog for version ${version}"

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
printf "# ${appName} v${version}\n\n## Qu'est-ce qui a changé ?\n${releaseNotesContent}\n\n[🚀 Voir la version sur GitHub](https://github.com/tchapgouv/tchap-x-android/releases/tag/v${version})\n\n"

# TCHAP - Disable matrix Bot token
read -r -p "Send the message manually, and press enter to continue. "
#if [[ -z "${elementBotToken}" ]]; then
#  read -r -p "ELEMENT_BOT_MATRIX_TOKEN is not defined in the environment. Cannot send the message for you. Please send it manually, and press enter to continue. "
#else
#  read -r -p "Send this message to the room (yes/no) default to yes? " doSend
#  doSend=${doSend:-yes}
#  if [ "${doSend}" == "yes" ]; then
#    printf "Sending message...\n"
#    transactionId=$(openssl rand -hex 16)
#    # Element Android internal
#    matrixRoomId="!LiSLXinTDCsepePiYW:matrix.org"
#    curl -X PUT --data "{\"msgtype\":\"m.text\",\"body\":\"${message}\"}" -H "Authorization: Bearer ${elementBotToken}" https://matrix-client.matrix.org/_matrix/client/r0/rooms/${matrixRoomId}/send/m.room.message/\$local."${transactionId}"
#  else
#    printf "Message not sent, please send it manually!\n"
#  fi
#fi

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
printf "Congratulation! Kudos for using this script! Have a nice day!\n"
printf "================================================================================\n"
