FROM java:8
VOLUME /tmp EXPOSE 8080
ADD ./target/spring-integregation-hub-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]