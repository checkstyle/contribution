FROM --platform=linux/amd64 cimg/base:2023.10

# Install dependencies for JDK 17 and Ant
RUN sudo apt update && \
    sudo apt install ca-certificates-java -y && \
    sudo apt install openjdk-17-jdk groovy jq ant -y

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV GROOVY_HOME=/usr/share/groovy
ENV PATH=$GROOVY_HOME/bin:$PATH

# Maven configuration
ENV MAVEN_VERSION=3.9.10
ENV PATH=/opt/apache-maven/bin:$PATH

# Install Maven
RUN MAVEN_3_URL=https://dlcdn.apache.org/maven/maven-3 && \
    MAVEN_VERSION_URL=$MAVEN_VERSION/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    URL=${MAVEN_3_URL}/${MAVEN_VERSION_URL} && \
    curl -sSL --fail --retry 3 "$URL" -o apache-maven.tar.gz && \
    sudo tar -xzf apache-maven.tar.gz -C /opt/ && \
    rm apache-maven.tar.gz && \
    sudo ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/apache-maven && \
    echo "JAVA_HOME=$JAVA_HOME" && \
    mvn --version
