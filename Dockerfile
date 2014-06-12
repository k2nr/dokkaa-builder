FROM ubuntu:14.04

MAINTAINER Kazunori Kajihiro "kazunori.kajihiro@gmail.com"

RUN apt-get update
RUN apt-get install -y openjdk-7-jre-headless curl
#RUN apt-get install supervisor -y

RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /bin/lein
RUN chmod a+x /bin/lein
ENV LEIN_ROOT true
RUN /bin/lein

RUN mkdir -p /app
WORKDIR /app
ADD src /app/src
ADD project.clj /app/project.clj
ADD resources /app/resources
RUN lein do cljsbuild clean, cljsbuild once release, uberjar

CMD ["java", "-jar", "target/dokkaa-builder-0.0.1-SNAPSHOT-standalone.jar"]