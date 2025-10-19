FROM anapsix/alpine-java:8u162b12_jdk

############################# Install tools and deps #########################################
RUN apk update && apk add --no-cache curl tar bash procps git openssh htop xmlstarlet libxslt
############################# End Install tools and deps #####################################

############################# Install Groovy ################################
ENV GROOVY_HOME /opt/groovy
ARG GROOVY_VERSION=5.0.0

RUN set -o errexit -o nounset && echo "Installing build dependencies" && \
    apk add --no-cache --virtual .build-deps ca-certificates gnupg openssl unzip && \
    echo "Downloading Groovy" && wget -O groovy.zip "https://dist.apache.org/repos/dist/release/groovy/${GROOVY_VERSION}/distribution/apache-groovy-binary-${GROOVY_VERSION}.zip" && \
    echo "Importing keys listed in http://www.apache.org/dist/groovy/KEYS from key server" && export GNUPGHOME="$(mktemp -d)" && for key in \
		"7FAA0F2206DE228F0DB01AD741321490758AAD6F" \
		"331224E1D7BE883D16E8A685825C06C827AF6B66" \
		"34441E504A937F43EB0DAEF96A65176A0FB1CD0B" \
		"9A810E3B766E089FFB27C70F11B595CEDC4AEBB5" \
		"81CABC23EECA0790E8989B361FF96E10F0E13706" \
	; do \
		for server in \
			"ha.pool.sks-keyservers.net" \
			"hkp://p80.pool.sks-keyservers.net:80" \
			"pgp.mit.edu" \
		; do \
			echo "  Trying ${server}"; \
			if gpg --keyserver "${server}" --recv-keys "${key}"; then \
				break; \
			fi; \
		done; \
	done; \
	if [ $(gpg --list-keys | grep -c "pub ") -ne 5 ]; then \
		echo "ERROR: Failed to fetch GPG keys" >&2; \
		exit 1; \
	fi \
	&& echo "Checking download signature" \
	&& wget -O groovy.zip.asc "https://dist.apache.org/repos/dist/release/groovy/${GROOVY_VERSION}/distribution/apache-groovy-binary-${GROOVY_VERSION}.zip.asc" \
	&& gpg --batch --verify groovy.zip.asc groovy.zip && rm -rf "${GNUPGHOME}" && rm groovy.zip.asc \
	&& echo "Installing Groovy" \
	&& unzip groovy.zip \
	&& rm groovy.zip \
	&& mkdir -p /opt \
	&& mv "groovy-${GROOVY_VERSION}" "${GROOVY_HOME}/" \
	&& ln -s "${GROOVY_HOME}/bin/grape" /usr/bin/grape \
	&& ln -s "${GROOVY_HOME}/bin/groovy" /usr/bin/groovy \
	&& ln -s "${GROOVY_HOME}/bin/groovyc" /usr/bin/groovyc \
	&& ln -s "${GROOVY_HOME}/bin/groovyConsole" /usr/bin/groovyConsole \
	&& ln -s "${GROOVY_HOME}/bin/groovydoc" /usr/bin/groovydoc \
	&& ln -s "${GROOVY_HOME}/bin/groovysh" /usr/bin/groovysh \
	&& ln -s "${GROOVY_HOME}/bin/java2groovy" /usr/bin/java2groovy \
	&& echo "Cleaning up build dependencies" && apk del .build-deps \
	&& echo "Symlinking root .groovy to groovy .groovy" \
	&& ln -s /home/groovy/.groovy /root/.groovy
############################# End Install Groovy ################################

############################# Install Maven ################################

ARG MAVEN_VERSION=3.5.3
ARG USER_HOME_DIR="/root"
ARG SHA=b52956373fab1dd4277926507ab189fb797b3bc51a2a267a193c931fffad8408
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha256sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
ENV M2_REPO "$MAVEN_CONF/repository"
############################# End Install Maven ################################

ENV CHECKSTYLE_HOME /usr/local/checkstyle/
WORKDIR $CHECKSTYLE_HOME
RUN set -x && cd $CHECKSTYLE_HOME/.. && \
    git clone https://github.com/checkstyle/checkstyle.git && cd checkstyle && \
    mvn dependency:go-offline && \
    mvn clean site -Pno-validations -Dmaven.test.failure.ignore=true && \
    mvn verify -DskipTests -Dmaven.test.failure.ignore=true || true && \
    mvn org.pitest:pitest-maven:mutationCoverage -DskipTests -Dmaven.test.failure.ignore=true || true && \
    # We don't fail the build on PMD/pitest/etc. warnings because the goal is to prefetch PMD/pitest/etc. Maven plugins into the image \
    rm -rf $CHECKSTYLE_HOME/*

CMD [ "echo", "To start build, run 'mvn ...' to start some Checkstyle build here" ]
