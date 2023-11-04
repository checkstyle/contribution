FROM cimg/base:2023.10

# ca-certificates-java must be installed AND configured before java + groovy installation
RUN sudo apt update && \
  sudo apt install ca-certificates-java && \
  sudo apt install openjdk-11-jdk groovy jq -y

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 \
    GROOVY_HOME=/usr/share/groovy \
    PATH=$GROOVY_HOME/bin:$PATH

ENV MAVEN_VERSION=3.9.5 \
    PATH=/opt/apache-maven/bin:$PATH

RUN MAVEN_3_URL=https://www.apache.org/dist/maven/maven-3 && \
    MAVEN_VERSION_URL=$MAVEN_VERSION/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    export URL=${MAVEN_3_URL}/${MAVEN_VERSION_URL} && \
    curl -sSL --fail --retry 3 $URL -o apache-maven.tar.gz && \
    sudo tar -xzf apache-maven.tar.gz -C /opt/ && \
    rm apache-maven.tar.gz && \
    sudo ln -s /opt/apache-maven-* /opt/apache-maven && \
    mvn --version
