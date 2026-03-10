pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = "637423625226"
        AWS_REGION     = "ap-northeast-2" 
        ECR_REGISTRY   = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        ECR_NAME      = "demo-ecr"
        IMAGE_TAG      = "${env.BUILD_NUMBER}" // 빌드 번호를 태그로 사용
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/limgyeol/main.git'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    sh "docker build -t ${ECR_NAME}:${IMAGE_TAG} ."
                    sh "docker tag ${ECR_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('ECR Login & Push') {
            steps {
                script {
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
                    
                    sh "docker push ${ECR_REGISTRY}/${ECR_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh "docker rmi ${ECR_REGISTRY}/${ECR_NAME}:${IMAGE_TAG}"
            }
        }
    }
}