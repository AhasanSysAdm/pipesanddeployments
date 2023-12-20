#!/bin/bash
aws ecr describe-images --repository-name ahasanecrrepo --query 'sort_by(imageDetails,& imagePushedAt)[-1].imageTags[0]' --output text | tail -c 3 > ecrimage-for-frontend.txt
cat ecrimage-for-frontend.txt | tr -d " \t\n\r" > outFile_k8simagedeploy