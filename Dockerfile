FROM androidsdk/android-28:latest

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . .

RUN bash -c 'cd /usr/src/app && ./gradlew'