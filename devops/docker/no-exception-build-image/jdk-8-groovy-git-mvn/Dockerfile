#################### Using official groovy image ####################
FROM groovy:jdk8
#################### End of images used ############################# 

##### Changing user to root to get access to /var/lib/dpkg/lock-frontend ########
USER root
##################### End of Change User #############################

##################### Installing Maven and git #######################
RUN apt-get update \
    && echo "Installing maven and git dependencies" \
    && apt-get install --yes --no-install-recommends \
        git \
        maven
##################### End of install and Dockerfile #################################

