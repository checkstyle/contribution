# Checkstyle's Docker

## Docker Image Release Workflow

To login to dockerhub:

```bash
docker login
```

To build image:

```bash
docker build -t <org>/<image-name>:<version> .
```

Where `version` is &lt;jdk-version&gt;-&lt;package-version&gt;.

Example:

```bash
docker build -t checkstyle/idea-docker:jdk11-idea2022.2.2 .
```

To push a new tag:

```bash
docker push <org>/<image-name>:<version>
```

Example:

```bash
docker push checkstyle/idea-docker:jdk11-idea2022.2.2
```

## Running Container on Local

To run in interactive mode:

```bash
docker run -it <org>/<image-name>:<version> /bin/bash
```

To access and run commands in the running container:

```bash
docker exec –it <container_id> /bin/bash
```
