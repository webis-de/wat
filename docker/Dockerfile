FROM openjdk:8-jre

ENV PORT=2112
ENV BASEPATH=/wat-sl
EXPOSE $PORT

RUN mkdir -p /srv/wat
ADD wat.jar /srv/wat
ADD demo-project.zip /srv/wat

WORKDIR /srv/wat
RUN unzip demo-project.zip
RUN rm demo-project.zip
RUN mv demo-project project

WORKDIR /srv/wat/project
CMD java -jar ../wat.jar $PORT $BASEPATH

