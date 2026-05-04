#!groovy
import hudson.tasks.test.AbstractTestResultAction
import hudson.model.Actionable

pipeline  {
    agent { node { label 'docker_linux' } }

    environment {
        IMAGE_NAME = "${env.BUILD_TAG.toLowerCase().replaceAll("\\s","").replaceAll("%2f","").replaceAll("\\\\","")}"
        BRANCH_NAME_CLEAN = "${env.BRANCH_NAME.replaceAll("%2f","").replaceAll("/","")}"
        PR_NUMBER = "${env.BRANCH_NAME_CLEAN.replace("PR-","")}"
        DOCKER_IMAGES_PREFIX = "${IMAGE_NAME}_"
        APP_VERSION = "${env.BRANCH_NAME_CLEAN}-${env.BUILD_NUMBER}"
        SLACK_CHANNEL = "#gts-jenkins"
        OUT_FOLDER = "${WORKSPACE}/out_${APP_VERSION}"
        // Hidora server using VPN ip: gts-fdroid.sh1.hidora.com
        FDROID_SERVER_HOST = '10.105.20.13'
        FDROID_SERVER_USER = 'fdroid'
        FDROID_SERVER_CREDENTIAL = 'HIDORA_PRIVATE_KEY_FDROID_SERVER'
        FDROID_SERVER_PROD_ROOT = "/var/www/store-prod/fdroid"
        FDROID_SERVER_PROD_REPO_ROOT = "${FDROID_SERVER_PROD_ROOT}/repo"
        FDROID_SERVER_UAT_ROOT = "/var/www/store-uat/fdroid"
        FDROID_SERVER_UAT_REPO_ROOT = "${env.FDROID_SERVER_UAT_ROOT}/repo"

        // OLD novelt server:
        /*
        FDROID_SERVER_HOST = '10.1.2.53'
        FDROID_SERVER_USER = 'novelt'
        FDROID_SERVER_FDROID_ROOT = '/home/novelt/htdocs/store'
        FDROID_SERVER_PROD_ROOT = "${FDROID_SERVER_FDROID_ROOT}/gts"
        FDROID_SERVER_PROD_REPO_ROOT = "${FDROID_SERVER_PROD_ROOT}/repo"
        FDROID_SERVER_UAT_ROOT = "${env.FDROID_SERVER_FDROID_ROOT}/gts-uat"
        FDROID_SERVER_UAT_REPO_ROOT = "${env.FDROID_SERVER_UAT_ROOT}/repo"
        */
        FDROID_PUBLISH_BRANCH = 'novelt/gts'
    }
    parameters {
        choice(name: 'TARGET_ENV', choices: 'Uat\nRelease', description: 'Publish to UAT or PROD store?')
    }
    stages {
        stage("Init") {
            steps {
                script {
                    GIT_COMMIT_USER = sh (
                      script: 'git show -s --pretty=%an',
                      returnStdout: true
                    ).trim()

                    GIT_MESSAGE = sh (
                      script: 'git log -1 --pretty=%B',
                      returnStdout: true
                    ).trim()

                    if (env.BRANCH_NAME == "${env.FDROID_PUBLISH_BRANCH}") {
                        env.PERFORM_FDROID_PUBLISH = true
                    }

                    sh "printenv"

                    sh """
                        echo Make sure OUT_FOLDER [${OUT_FOLDER}] is empty
                        rm -rf "${OUT_FOLDER}"
                        mkdir -p "${OUT_FOLDER}"
                    """
                }
            }
        }
        stage('Build images') {
            environment {
                NOVELT_KEYSTORE = credentials('NOVELT_ANDROID_KEYSTORE')
                // Note: this file is currently empty but needed for building, see: secrets.gradle
                SECRETS_PROPERTIES = credentials('GTS_COLLECT_SECRETS_PROPERTIES')
            }
            steps {
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                    sh '''
                        rm -f ./collect_app/novelt-android.keystore
                        rm -f ./collect_app/secrets.properties
                        cp "${NOVELT_KEYSTORE}" ./collect_app/novelt-android.keystore
                        cp "${SECRETS_PROPERTIES}" ./collect_app/secrets.properties
                        scripts/docker/build.sh
                    '''
                }
            }
            post {
                failure {
                    slackSend channel: "${SLACK_CHANNEL}", color:"danger", message:"""GTS collect - Build docker images failed - <https://github.com/novelt/collect/pull/${env.PR_NUMBER}|${env.BRANCH_NAME}> ${env.BUILD_NUMBER} (<https://jenkins.novel-t.ch/job/GTS-PIPELINES/job/${env.BRANCH_NAME}|Open Jenkins>)
                    Commit by: ${GIT_COMMIT_USER}
                    Message: ${GIT_MESSAGE}
                    """
                }
            }
        }
//         stage('Perform UAT Tests') {
//             steps {
//                 wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
//                     sh '''
//                         docker run "${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" bash -c "./gradlew clean && ./gradlew testUatUnitTest"
//                     '''
//
//                     sh '''
//                         DOCKER_SOURCE_CONTAINER=$(docker ps -qa --filter="ancestor=${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" --last 1)
//                         docker cp "${DOCKER_SOURCE_CONTAINER}:/usr/src/app/app/build/test-results/testUatUnitTest" "${OUT_FOLDER}/"
//                         docker rm $(docker ps --filter=name=${DOCKER_IMAGES_PREFIX} -qa) > /dev/null 2>&1 || true
//                     '''
//                 }
//             }
//             post {
// //                 always {
// //                     junit "${OUT_FOLDER}/**/*.ml"
// //                 }
//                 failure {
//                     slackSend channel: "${SLACK_CHANNEL}", color:"danger", message:"""Build Failed - <https://github.com/novelt/GTS/pull/${env.PR_NUMBER}|${env.BRANCH_NAME}> ${env.BUILD_NUMBER} (<http://jenkinsm01.ad.novel-t.ch:8080/job/GTS/job/${env.BRANCH_NAME}|Open Jenkins>)
//                     Commit by: ${GIT_COMMIT_USER}
//                     Message: ${GIT_MESSAGE}
//                     """
//                 }
//             }
//         }
//         stage('Perform Release Tests') {
//             steps {
//                 wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
//                     sh '''
//                         docker run "${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" bash -c "./gradlew clean && ./gradlew testReleaseUnitTest"
//                     '''
//
//                     sh '''
//                         DOCKER_SOURCE_CONTAINER=$(docker ps -qa --filter="ancestor=${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" --last 1)
//                         docker cp "${DOCKER_SOURCE_CONTAINER}:/usr/src/app/app/build/test-results/testReleaseUnitTest" "${OUT_FOLDER}/"
//                         docker rm $(docker ps --filter=name=${DOCKER_IMAGES_PREFIX} -qa) > /dev/null 2>&1 || true
//                     '''
//                 }
//             }
//             post {
// //                 always {
// //                     junit "${OUT_FOLDER}/**/*.ml"
// //                 }
//                 failure {
//                     slackSend channel: "${SLACK_CHANNEL}", color:"danger", message:"""Build Failed - <https://github.com/novelt/GTS/pull/${env.PR_NUMBER}|${env.BRANCH_NAME}> ${env.BUILD_NUMBER} (<http://jenkinsm01.ad.novel-t.ch:8080/job/GTS/job/${env.BRANCH_NAME}|Open Jenkins>)
//                     Commit by: ${GIT_COMMIT_USER}
//                     Message: ${GIT_MESSAGE}
//                     """
//                 }
//             }
//         }
        stage('Build APKs') {
            steps {
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                    sh '''
                        docker run "${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" bash -c "./gradlew clean && ./gradlew assembleGtsUat assembleGtsRelease"
                    '''

                    sh '''
                        DOCKER_SOURCE_CONTAINER=$(docker ps -qa --filter="ancestor=${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" --last 1)
                        docker cp "${DOCKER_SOURCE_CONTAINER}:/usr/src/app/collect_app/build/outputs/apk" "${OUT_FOLDER}/"
                        docker rm $(docker ps --filter=name=${DOCKER_IMAGES_PREFIX} -qa) > /dev/null 2>&1 || true
                    '''
                }
            }
            post {
                failure {
                    slackSend channel: "${SLACK_CHANNEL}", color:"danger", message:"""GTS collect - Build APKs failed - <https://github.com/novelt/collect/pull/${env.PR_NUMBER}|${env.BRANCH_NAME}> ${env.BUILD_NUMBER} (<https://jenkins.novel-t.ch/job/GTS-PIPELINES/job/${env.BRANCH_NAME}|Open Jenkins>)
                    Commit by: ${GIT_COMMIT_USER}
                    Message: ${GIT_MESSAGE}
                    """
                }
            }
        }
        stage('Publish to FDroid UAT') {
            when {
                allOf {
                    environment name: "PERFORM_FDROID_PUBLISH", value: "true"
                    environment name: "TARGET_ENV", value: "Uat"
                }
            }
            steps {
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                    sshagent([FDROID_SERVER_CREDENTIAL]) {
                        // UAT STORE
                        sh '''
                            set -x
                            uat_apk_file_path=$(find ${OUT_FOLDER}/apk/gts/uat/*.apk -type f)
                            uat_apk_filename=$(basename -- "${uat_apk_file_path}")
                            if [ -f ${uat_apk_file_path} ]; then
                              scp -o StrictHostKeyChecking=no ${uat_apk_file_path} "${FDROID_SERVER_USER}@${FDROID_SERVER_HOST}:${FDROID_SERVER_UAT_REPO_ROOT}/"
                              ssh -o StrictHostKeyChecking=no "${FDROID_SERVER_USER}@${FDROID_SERVER_HOST}" ln -sf ${FDROID_SERVER_UAT_ROOT}/repo/${uat_apk_filename} ${FDROID_SERVER_UAT_ROOT}/GTSCollect.apk
                            fi
                            ssh -o StrictHostKeyChecking=no "${FDROID_SERVER_USER}@${FDROID_SERVER_HOST}" "cd ${FDROID_SERVER_UAT_ROOT} && fdroid update"
                        '''
                    }
                }
            }
            post {
                failure {
                    slackSend channel: "${SLACK_CHANNEL}", color:"danger", message:"""GTS collect - Publish to FDroid UAT failed - <https://github.com/novelt/collect/pull/${env.PR_NUMBER}|${env.BRANCH_NAME}> ${env.BUILD_NUMBER} (<https://jenkins.novel-t.ch/job/GTS-PIPELINES/job/${env.BRANCH_NAME}|Open Jenkins>)
                    Commit by: ${GIT_COMMIT_USER}
                    Message: ${GIT_MESSAGE}
                    """
                }
            }
        }

        stage('Publish to FDroid PROD') {
            when {
                allOf {
                    environment name: "PERFORM_FDROID_PUBLISH", value: "true"
                    environment name: "TARGET_ENV", value: "Release"
                }
            }
            steps {
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                    sshagent([FDROID_SERVER_CREDENTIAL]) {
                        // PROD STORE
                        sh '''
                            set -x
                            release_apk_file_path=$(find ${OUT_FOLDER}/apk/gts/release/*.apk -type f)
                            release_apk_filename=$(basename -- "${release_apk_file_path}")
                            if [ -f ${release_apk_file_path} ]; then
                              scp -o StrictHostKeyChecking=no ${release_apk_file_path} "${FDROID_SERVER_USER}@${FDROID_SERVER_HOST}:${FDROID_SERVER_PROD_REPO_ROOT}/"
                              ssh -o StrictHostKeyChecking=no "${FDROID_SERVER_USER}@${FDROID_SERVER_HOST}" ln -sf ${FDROID_SERVER_PROD_ROOT}/repo/${release_apk_filename} ${FDROID_SERVER_PROD_ROOT}/GTSCollect.apk
                            fi
                        '''
                    }
                }
            }
            post {
                failure {
                    slackSend channel: "${SLACK_CHANNEL}", color:"danger", message:"""GTS collect - Publish to FDroid failed - <https://github.com/novelt/collect/pull/${env.PR_NUMBER}|${env.BRANCH_NAME}> ${env.BUILD_NUMBER} (<https://jenkins.novel-t.ch/job/GTS-PIPELINES/job/${env.BRANCH_NAME}|Open Jenkins>)
                    Commit by: ${GIT_COMMIT_USER}
                    Message: ${GIT_MESSAGE}
                    """
                }
            }
        }
    }
    post {
        cleanup {
            sh '''
                docker stop $(docker ps --filter=name="${DOCKER_IMAGES_PREFIX}" -q) > /dev/null 2>&1 || true
                docker rm $(docker ps --filter=name="${DOCKER_IMAGES_PREFIX}" -qa) > /dev/null 2>&1 || true
                docker image rm -f $(docker image list --filter=reference="${DOCKER_IMAGES_PREFIX}*" -q) > /dev/null 2>&1 || true
                rm -rf "${OUT_FOLDER}"
            '''
        }
    }
}
