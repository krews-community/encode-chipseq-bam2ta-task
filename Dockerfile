FROM openjdk:8-jre-slim as base
RUN apt-get update && apt-get install -y \
    build-essential \
    wget \
    liblzma-dev \
    libbz2-dev \
    zlib1g-dev \
    libstdc++ \
    python \
    openssl \
    bash \
    git && \
    wget https://github.com/biod/sambamba/releases/download/v0.6.8/sambamba-0.6.8-linux-static.gz && \
    gunzip sambamba-0.6.8-linux-static.gz && mv sambamba-0.6.8-linux-static /bin/sambamba && chmod 755 /bin/sambamba && \
    git clone https://github.com/arq5x/bedtools2 && cd bedtools2 && make && mv bin/bedtools /bin/bedtools && \
    cd .. && rm -r bedtools2 && \
    apt-get purge --auto-remove -y  git build-essential wget

FROM openjdk:8-jdk-alpine as build
COPY . /src
WORKDIR /src

RUN ./gradlew clean shadowJar

FROM base
RUN mkdir /app
COPY --from=build /src/build/chipseq-bam2ta-*.jar /app/chipseq.jar