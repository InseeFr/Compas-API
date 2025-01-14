FROM gitlab-registry.insee.fr:443/kubernetes/images/run/jre:21-rootless
# Copie du Jar avec les bons droits 
COPY --chown=$JAVA_USER:$JAVA_USER target/*.jar /usr/local/app.jar
RUN ls -alh /usr/local/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/app.jar"]
