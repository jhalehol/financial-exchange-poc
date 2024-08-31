FROM maven:3.6-jdk-8

COPY docker/assets/*.sh /app/
COPY *.sh /app/
RUN chmod +x /app/*.sh

COPY target/financial-spring-boot.jar /app/
COPY key /app/
COPY key_.pub /app/

WORKDIR /app

EXPOSE 9200

ENTRYPOINT ["./entrypoint.sh"]
