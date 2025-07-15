#!/bin/bash
set -euo pipefail

# ----------------------------------------
# deploy.sh
# ----------------------------------------

# 1. 로그 디렉터리 생성
mkdir -p /home/ubuntu/logs

# 2. 배포 디렉터리로 이동
cd /home/ubuntu/deploy

# 3. SSM 파라미터에서 Spring Boot 환경변수 로드
echo "[INFO] Fetching Spring Boot environment variables from SSM..."
#   - /spring/prod/ 경로 아래 파라미터를 모두 불러와서 .env 파일로 생성
#   - Name은 마지막 슬래시 기준으로 잘라서, '.' → '_' → 대문자 변환
aws ssm get-parameters-by-path \
  --path "/spring/prod/" \
  --with-decryption \
  --query "Parameters[*].{Name:Name,Value:Value}" \
  --output text | while read name value; do
    key=$(basename "$name" | tr '.' '_' | tr '[:lower:]' '[:upper:]')
    echo "$key=$value" >> .env
done

echo "[INFO] .env file generated:"
cat .env

# 4. 기존 컨테이너 종료 및 리소스 정리
echo "[INFO] Shutting down existing containers (if any)..."
docker compose down || true

# 5. ECR 로그인 (이미지 풀 전)
echo "[INFO] Logging in to ECR..."
aws ecr get-login-password --region ap-northeast-2 \
  | docker login --username AWS --password-stdin 266735804784.dkr.ecr.ap-northeast-2.amazonaws.com

# 6. 최신 이미지를 ECR에서 풀 (올바른 레포 이름: app-repo)
echo "[INFO] Pulling latest image from ECR..."
docker pull 266735804784.dkr.ecr.ap-northeast-2.amazonaws.com/app-repo:latest

# 7. Docker Compose로 애플리케이션 및 Redis 컨테이너 실행
echo "[INFO] Starting up containers with Docker Compose..."
docker compose up -d

echo "[INFO] Deployment complete."
