FROM openjdk:8-jre

ENV PORT=2112
ENV BASEPATH=/wat-sl
EXPOSE $PORT

RUN mkdir -p /srv/wat
ADD wat.jar /srv/wat
ADD example-project.zip /srv/wat

WORKDIR /srv/wat
RUN unzip example-project.zip
RUN rm example-project.zip
RUN mv example-project project

WORKDIR /srv/wat/project
CMD java -jar ../wat.jar $PORT $BASEPATH

