pipeline {
    agent any

    environment{
        SOURCE_PATH="${WORKSPACE}/client" 
        DEPLOY_ENV="test"
        CLUSTER="test-cluster"
        ECR_REPO="843390005801.dkr.ecr.us-west-2.amazonaws.com"
        REPO_NAME = "mb-batch-request-service-rc"
        IMAGE_NAME="mb"
        TAG="frontend-${DEPLOY_ENV}-${BUILD_NUMBER}"
        AWS_REGION="us-west-2"
        EKS_CLUSTER_NAME = "your-eks-cluster-name"
   }

    stages {
        stage('Pull Code') {
            steps {
                git 'https://github.com/AhasanSysAdm/mern-app.git'
            }
        }

        stage('Image Build') {
            steps {
                 sh '''
                    docker build -t $ECR_REPO/$IMAGE_NAME:$TAG $WORKSPACE -f $SOURCE_PATH/Dockerfile
                '''
            }
            post {
                success {
                 
                  sh '''
                    echo "Build Completed"
                  
                  '''
                 
                }
            }
        }

        stage('ECR-Login') {
            steps {
                script{
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '78d7a8d4-4cca-4815-b8dc-0eaaba4be402',usernameVariable: 'ACCESSKEY', passwordVariable: 'SECRETKEY']]){
                    sh '''
                        export AWS_ACCESS_KEY_ID=$ACCESSKEY
                        export AWS_SECRET_ACCESS_KEY=$SECRETKEY
                        export AWS_DEFAULT_REGION=$AWS_REGION
                        /usr/local/bin/aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPO
                    
                    '''
                    }
                }                 
            }

            post {
                success {
                 
                 sh '''
                    echo "ECR LOGIN DONE"
                  
                  '''
                 
                }
            }
        }

        stage('Image-Push') {
            steps {
               sh '''
                  docker push $ECR_REPO/$IMAGE_NAME:$TAG
                  
                  '''
            }
        }
        stage('EKS-Update') {
            steps {
               script{
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'awsp',usernameVariable: 'ACCESSKEY', passwordVariable: 'SECRETKEY']]){
                        
                        sh '''
                            bash $SCRIPT_PATH/ecs-update.sh $ACCESSKEY $SECRETKEY $AWS_REGION $CLUSTER $SERVICE_NAME $ECR_REPO $IMAGE_NAME $TAG $ENV_PATH $LOG_GROUP $CPU $RAM
                            bash ${SCRIPT_PATH}/ecrbuildnumscript.sh'
                            buildtemp = readFile 'outFile_batchupdate_ml'
                            echo "The image is ${buildtemp}"
                        '''
                    }
                }
            }
        }
    }
}
