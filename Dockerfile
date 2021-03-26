FROM adoptopenjdk:11-jre-hotspot
COPY build/libs/eszett-*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]