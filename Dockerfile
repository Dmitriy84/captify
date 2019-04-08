FROM openjdk:8
VOLUME /tmp
ENV _JAVA_OPTIONS "-Xms256m -Xmx512m -Djava.awt.headless=true"

# Update aptitude with new repo && Install software
RUN apt-get update && apt-get install -y git

# Install maven
RUN apt-get install -y maven

WORKDIR /opt
# Clone new version of the repo
RUN git clone https://github.com/Dmitriy84/captify.git

#RUN addgroup bootapp && \
#    adduser -disabled-password -S -h /var/cache/bootapp -s /sbin/nologin -G bootapp bootapp
#USER bootapp

WORKDIR /opt/captify
# Download all dependencies
RUN mvn -s settings.xml dependency:go-offline compile

ENTRYPOINT ["mvn", "-Djava.security.egd=file:/dev/./urandom", "exec:java"]