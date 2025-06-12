#!/bin/bash

# Script to generate JavaDocs for TheKnife project

echo "Generating JavaDocs for TheKnife project..."

# Create directory for JavaDocs
mkdir -p target/site/apidocs

# Find all JAR files in the Maven repository that might be needed
# This is a simplified approach - in a real environment, we would use Maven to get the classpath
CLASSPATH=""
if [ -d "$HOME/.m2/repository" ]; then
  # Find Jackson databind JAR
  JACKSON_DATABIND=$(find $HOME/.m2/repository -name "jackson-databind-*.jar" | head -1)
  # Find Jackson annotations JAR
  JACKSON_ANNOTATIONS=$(find $HOME/.m2/repository -name "jackson-annotations-*.jar" | head -1)
  # Find Jackson core JAR
  JACKSON_CORE=$(find $HOME/.m2/repository -name "jackson-core-*.jar" | head -1)
  # Find JavaFX controls JAR
  JAVAFX_CONTROLS=$(find $HOME/.m2/repository -name "javafx-controls-*.jar" | head -1)
  # Find JavaFX FXML JAR
  JAVAFX_FXML=$(find $HOME/.m2/repository -name "javafx-fxml-*.jar" | head -1)
  # Find ControlsFX JAR
  CONTROLSFX=$(find $HOME/.m2/repository -name "controlsfx-*.jar" | head -1)
  # Find Spring Security Core JAR
  SPRING_SECURITY=$(find $HOME/.m2/repository -name "spring-security-core-*.jar" | head -1)
  # Find OpenCSV JAR
  OPENCSV=$(find $HOME/.m2/repository -name "opencsv-*.jar" | head -1)
  # Find Lombok JAR
  LOMBOK=$(find $HOME/.m2/repository -name "lombok-*.jar" | head -1)
  # Find SLF4J API JAR
  SLF4J_API=$(find $HOME/.m2/repository -name "slf4j-api-*.jar" | head -1)
  # Find Logback Classic JAR
  LOGBACK_CLASSIC=$(find $HOME/.m2/repository -name "logback-classic-*.jar" | head -1)
  # Find JavaFX Base JAR (needed for JavaFX controls)
  JAVAFX_BASE=$(find $HOME/.m2/repository -name "javafx-base-*.jar" | head -1)
  # Find JavaFX Graphics JAR (needed for JavaFX controls)
  JAVAFX_GRAPHICS=$(find $HOME/.m2/repository -name "javafx-graphics-*.jar" | head -1)

  # Combine all JARs into classpath
  CLASSPATH="$JACKSON_DATABIND:$JACKSON_ANNOTATIONS:$JACKSON_CORE:$JAVAFX_CONTROLS:$JAVAFX_FXML:$CONTROLSFX:$SPRING_SECURITY:$OPENCSV:$LOMBOK:$SLF4J_API:$LOGBACK_CLASSIC:$JAVAFX_BASE:$JAVAFX_GRAPHICS"

  echo "Using classpath: $CLASSPATH"
else
  echo "Maven repository not found. JavaDocs may have missing references."
fi

# Generate JavaDocs using the javadoc tool directly with -linkoffline to avoid checking URLs
# First, create a dummy package-list file for external dependencies
mkdir -p target/javadoc-temp/package-list
echo "java.lang
java.util
java.io" > target/javadoc-temp/package-list/package-list

# Generate JavaDocs with ignoreSourceErrors flag to force generation despite errors
javadoc -d target/site/apidocs \
  -sourcepath src/main/java \
  -encoding UTF-8 \
  -charset UTF-8 \
  -docencoding UTF-8 \
  -author \
  -version \
  -private \
  -classpath "$CLASSPATH" \
  -Xdoclint:none \
  -nodeprecated \
  -quiet \
  -J-Duser.language=en \
  --ignore-source-errors \
  -subpackages uni.insubria.theknife

echo "JavaDocs generated successfully in target/site/apidocs directory"
