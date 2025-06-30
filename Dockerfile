# 1️⃣ 빌드 스테이지: Microsoft OpenJDK 21 (Ubuntu 기반)
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS builder

WORKDIR /build

# Gradle 프로젝트 복사
COPY Koco/ .

# Gradle Wrapper로 빌드, 테스트 제외
RUN ./gradlew clean build -x test


# 2️⃣ 런타임 스테이지: Microsoft OpenJDK 21 JRE (Ubuntu 기반)
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

# Scouter 버전 지정
ARG SCOUTER_VERSION=2.20.0
ENV SCOUTER_VERSION=${SCOUTER_VERSION}

# 필요한 패키지 설치 및 Scouter 다운로드/압축 해제
RUN apt-get update && \
    apt-get install -y wget tar && \
    cd /opt && \
    wget https://github.com/scouter-project/scouter/releases/download/v${SCOUTER_VERSION}/scouter-all-${SCOUTER_VERSION}.tar.gz && \
    tar -xzf scouter-all-${SCOUTER_VERSION}.tar.gz && \
    rm scouter-all-${SCOUTER_VERSION}.tar.gz


# Host Agent 설정
RUN mkdir -p /opt/scouter/agent.host/conf && \
    echo 'net_collector_ip=10.1.3.100\n\
    net_collector_udp_port=6100\n\
    net_collector_tcp_port=6100' > /opt/scouter/agent.host/conf/scouter.conf

# Java Agent 설정
RUN mkdir -p /opt/scouter/agent.java/conf && \

    echo 'net_collector_ip=10.3.3.100\n\
    net_collector_udp_port=6100\n\
    net_collector_tcp_port=6100' > /opt/scouter/agent.java/conf/scouter.conf


# Spring Boot JAR 복사
WORKDIR /app
COPY --from=builder /build/build/libs/*-SNAPSHOT.jar app.jar

# 컨테이너 포트 오픈
EXPOSE 8080

# Java Agent 설정 포함 ENTRYPOINT
ENTRYPOINT ["java", \
  "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
  "-javaagent:/opt/scouter/agent.java/scouter.agent.jar", \
  "-Dscouter.config=/opt/scouter/agent.java/conf/scouter.conf", \
  "-jar", "app.jar"]
