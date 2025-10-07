FROM --platform=linux/amd64 cimg/base:2023.10

# Install dependencies
RUN sudo apt update && \
    sudo apt install -y jq ant curl unzip zip gzip tar bash git && \
    sudo rm -rf /var/lib/apt/lists/*

# Install SDKMAN
ENV SDKMAN_DIR=/home/circleci/.sdkman
RUN curl -s "https://get.sdkman.io" | bash

# Install Java 21, Groovy, Maven via SDKMAN
RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && \
    sdk install java 21.0.4-tem && \
    sdk install groovy 5.0.0 && \
    sdk install maven 3.9.11"

# Update PATH and setup environment
ENV JAVA_HOME=$SDKMAN_DIR/candidates/java/current
ENV GROOVY_HOME=$SDKMAN_DIR/candidates/groovy/current
ENV PATH=$GROOVY_HOME/bin:$SDKMAN_DIR/candidates/maven/current/bin:$JAVA_HOME/bin:$PATH

# Verify installation
RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && \
    java -version && \
    groovy --version && \
    mvn -version"
