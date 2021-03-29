FROM openjdk:11.0.2
COPY target/testingService-*.jar testingService.jar
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/absService.jar", "--spring.config.location=classpath:application-dev.yaml"]