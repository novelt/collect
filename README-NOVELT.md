# How to use locally

## NOTES
- Some Tests are currently failing -> Tests are deactivated in the Jenkinsfile
- Once the tests pass, you can uncomment `Perform UAT Tests` and `Perform Release Tests` stages in `Jenkinsfile`
- There are also warnings when building the UAT & Release APK's
- You should take a look at
  - `./gradlew testGtsReleaseUnitTest` <- Errors
  - `./gradlew testGtsUatUnitTest` <- Errors
  - `./gradlew assembleGtsUat` <- Warnings
  - `./gradlew assembleGtsRelease` <- Warnings

## Prepare your environment

- Copy `collect_app/secrets.properties.template` to `collect_app/secrets.properties`
- Edit `collect_app/secrets.properties` and set appropriate values for each variable
- Get `novelt-android.keystore` (DropBox or KeePass) and copy it to `collect_app/novelt-android.keystore`
  - If the KeyStore is available elsewhere on your filesystem (ie in a Dropbox local path) AND that you won't use the Docker container, just set `RELEASE_STORE_FILE` the to full filename of the KeyStore
  
## Docker (in WSL)

### Build the Docker Image

Note: You may have to `chmod +x scripts/docker/build.sh`

```shell
cd <repo-root> # This is VERY IMPORTANT ; pwd must be <repo-root> when you run the script. ie. DO NOT cd scripts/docker && build.sh
source config/local.env
scripts/docker/build.sh
```

### Test & Build the APK's

Note: You can comment appropriate `docker run` commands in `scripts/test-and-build-apks-local.sh` to run only the steps you want.

Note, you may have to make the script executable by running `chmod +x scripts/test-and-build-apks-local.sh`.

```shell
scripts/test-and-build-apks-local.sh
```

### Get a shell in the container
- Build the image (see above)
- docker run -it "${DOCKER_IMAGES_PREFIX}gts_mobile:${APP_VERSION}" bash
- now you can execute commands like `./gradlew testGtsReleaseUnitTest` (mind the `./`)

### Override some settings
- If you need to build an APK with some specific settings you can do it this way :
- Update classes GtsFormEntryActivity and GtsSplashScreenActivity : (the example below force the navigation setting to 'swipe_buttons')
```
GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_NAVIGATION, "swipe_buttons");
```