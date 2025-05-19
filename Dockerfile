FROM mobiledevops/android-sdk-image:34.0.0-jdk17

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . .

#COPY ./novelt-android.keystore ./collect_app/

RUN bash -c 'cd /usr/src/app && ./gradlew'
