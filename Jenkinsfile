pipeline {
  agent any
  stages {
    stage('Maven Build') {
      agent {
        docker {
          image 'maven:3-jdk-8-alpine'
          args '-v $HOME/.m2:/root/.m2'
        }

      }
      when {
        expression {
          env.GIT_TAG != null
        }

      }
      steps {
        sh 'mvn clean package -Dfile.encoding=UTF-8 -DskipTests=true'
        stash(includes: 'target/*.jar', name: 'app')
      }
    }

    stage('Docker Build') {
      agent any
      when {
        allOf {
          expression {
            env.GIT_TAG != null
          }

        }

      }
      steps {
        unstash 'app'
        sh "docker login -u ${HARBOR_CREDS_USR} -p ${HARBOR_CREDS_PSW} ${params.HARBOR_HOST}"
        sh "docker build --build-arg JAR_FILE=`ls target/*.jar |cut -d '/' -f2` -t ${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${GIT_TAG} ."
        sh "docker push ${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${GIT_TAG}"
        sh "docker rmi ${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${GIT_TAG}"
      }
    }

    stage('Deploy') {
      agent {
        docker {
          image 'lwolf/helm-kubectl-docker'
        }

      }
      when {
        allOf {
          expression {
            env.GIT_TAG != null
          }

        }

      }
      steps {
        sh 'mkdir -p ~/.kube'
        sh "echo ${K8S_CONFIG} | base64 -d > ~/.kube/config"
        sh "sed -e 's#{IMAGE_URL}#${params.HARBOR_HOST}/${params.DOCKER_IMAGE}#g;s#{IMAGE_TAG}#${GIT_TAG}#g;s#{APP_NAME}#${params.APP_NAME}#g;s#{SPRING_PROFILE}#k8s-test#g' k8s-deployment.tpl > k8s-deployment.yml"
        sh "kubectl apply -f k8s-deployment.yml --namespace=${params.K8S_NAMESPACE}"
      }
    }

  }
  environment {
    HARBOR_CREDS = credentials('jenkins-harbor-creds')
    K8S_CONFIG = credentials('jenkins-k8s-config')
    GIT_TAG = sh(returnStdout: true,script: 'git describe --tags --always').trim()
  }
  parameters {
    string(name: 'HARBOR_HOST', defaultValue: '127.222.186.208:8084', description: 'harbor仓库地址')
    string(name: 'DOCKER_IMAGE', defaultValue: 'xh13k/cleaning_auntie', description: 'docker镜像名')
    string(name: 'APP_NAME', defaultValue: 'cleaning_auntie', description: 'k8s中标签名')
    string(name: 'K8S_NAMESPACE', defaultValue: 'xh13k', description: 'k8s的namespace名称')
  }
}