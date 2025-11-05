FROM gitlab-registry.insee.fr:443/kubernetes/images/run/java:25-rootless-jre
COPY target/*.jar /usr/local/app.jar
EXPOSE 8080
CMD ["java", "-jar","/usr/local/app.jar"]