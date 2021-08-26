FROM androidsdk/android-28:latest

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . .

#COPY ./novelt-android.keystore ./collect_app/

RUN bash -c 'cd /usr/src/app && ./gradlew'