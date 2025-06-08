FROM cimg/base:2023.10

RUN sudo apt update && \
    sudo apt install -y ca-certificates-java unzip && \
    sudo apt install -y openjdk-17-jdk groovy jq

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
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

# Install SDKMAN for Groovy version management
RUN curl -s "https://get.sdkman.io" | bash && \
    chmod +x "$HOME/.sdkman/bin/sdkman-init.sh"

RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && \
    sdk install groovy 2.4.21 && \
    groovy -version"

ENV PATH="$HOME/.sdkman/candidates/groovy/current/bin:$PATH"
