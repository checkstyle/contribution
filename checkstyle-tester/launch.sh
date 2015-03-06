SOURCES_DIR=src/main/java

echo "Testing Checkstyle started"

cat projects-to-test-on.properties | while read line; do 

    [[ "$line" =~ ^#.*$ ]] && continue # Skip lines with comments
    
    REPO_NAME=`echo $line | cut -d '=' -f 1`
    REPO_URL=`echo $line | cut -d '=' -f 2`
    
    REPO_SOURCES_DIR=$SOURCES_DIR/$REPO_NAME
    
    if [ -d "$REPO_SOURCES_DIR" ]; then
      echo "Pulling repository '$REPO_NAME' ..."
	  cd $REPO_SOURCES_DIR
	  git pull
	  cd -
	  echo "Pulling repository '$REPO_NAME' - successful"
	else
	  echo "Cloning repository '$REPO_NAME' ..."
      git clone $REPO_URL $REPO_SOURCES_DIR
      echo "Cloning repository '$REPO_NAME' - successful"
	fi

done

if [ -d "target" ]; then
  echo "Existing target directory will be cleared"
  rm -rf target/*
else
  mkdir "target"
fi

mvn --batch-mode clean

echo "Running Checkstyle on $SOURCES_DIR ..."
time mvn --batch-mode site "$@" # > target/console_log.txt
echo "Running Checkstyle on $SOURCES_DIR - finished"

echo "Testing Checkstyle finished"
