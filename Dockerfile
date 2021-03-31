FROM openjdk:14.0.2
COPY target/testingService-*.jar testingService.jar
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/testingService.jar"]
