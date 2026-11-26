# Compilarmos
./gradlew build

# Ejecutamos la aplicación
./gradlew bootRun --args='--server.address=127.0.0.1'

# Hacemos una petición a la aplicación
curl -i http://127.0.0.1:8080/api/status

# Identificamos la dependencia vulnerable
./gradlew dependencyInsight \
  --dependency log4j-core \
  --configuration compileClasspath

# Compilamos una muestra de la vulnerabilidad
javac \
      -cp log4j-api-2.14.1.jar \
      -d /tmp/classes \
      LookupBehavior.java

# Compilamos una muestra de la vulnerabilidad
java \
      -cp "/tmp/classes:log4j-api-2.14.1.jar:log4j-core-2.14.1.jar" \
      LookupBehavior

#####################################################
# Compilamos una muestra de la vulnerabilidad con la versión parcheada
javac \
      -cp log4j-api-2.25.5.jar \
      -d /tmp/classes \
      LookupBehavior.java

# Compilamos una muestra de la vulnerabilidad con la versión parcheada
java \
      -cp "/tmp/classes:log4j-api-2.25.5.jar:log4j-core-2.25.5.jar" \
      LookupBehavior

# Generación del SBOM 
./gradlew cyclonedxBom

# Configurar en GitHub
# Dependabot
# Dependency Graph
# Automatic dependency submission
# Dependabot security updates

