pipeline {
    agent any

    environment {
        // AWS 계정 ID와 리전 설정
        AWS_ACCOUNT_ID = "637423625226"
        AWS_REGION     = "ap-northeast-2" 
        ECR_REGISTRY   = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        REPO_NAME      = "my-app-repo"
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
                    // Docker 이미지 빌드
                    sh "docker build -t ${REPO_NAME}:${IMAGE_TAG} ."
                    // ECR용 풀 네임으로 태깅
                    sh "docker tag ${REPO_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${REPO_NAME}:${IMAGE_TAG}"
                    sh "docker tag ${REPO_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${REPO_NAME}:latest"
                }
            }
        }

        stage('ECR Login & Push') {
            steps {
                script {
                    // AWS CLI v2 방식을 사용하여 ECR 로그인
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
                    
                    // 이미지 푸시
                    sh "docker push ${ECR_REGISTRY}/${REPO_NAME}:${IMAGE_TAG}"
                    sh "docker push ${ECR_REGISTRY}/${REPO_NAME}:latest"
                }
            }
        }

        stage('Cleanup') {
            steps {
                // 용량 관리를 위해 로컬에 생성된 이미지 삭제 (선택 사항)
                sh "docker rmi ${ECR_REGISTRY}/${REPO_NAME}:${IMAGE_TAG}"
                sh "docker rmi ${ECR_REGISTRY}/${REPO_NAME}:latest"
            }
        }
    }
}