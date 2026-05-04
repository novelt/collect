FROM mobiledevops/android-sdk-image:34.0.0-jdk17

# parent Dockerfile sets USER to mobiledevops
USER root
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . .
RUN chown -R mobiledevops:mobiledevops .

USER mobiledevops

#COPY ./novelt-android.keystore ./collect_app/

RUN bash -c 'cd /usr/src/app && ./gradlew'
