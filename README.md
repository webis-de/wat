WAT-SL
======
Web Annotation Tool for Segment Labeling.

Demo server
-----------
For a quick look into WAT-SL, please visit [our demo server](https://demo.webis.de/wat-sl/).


Usage
-----
Download the [wat.jar](https://github.com/webis-de/wat/releases/download/1.1.1/wat.jar). Then unpack the [example-project.zip](https://github.com/webis-de/wat/releases/download/1.1.0/example-project.zip) and cd into the example project folder. Then use

    java -jar <path-to>/wat.jar [<port> [<base-path>]]

to start the server on the given port (default port is 2112) and base-path (default base-path is "/"). You can then access documentation on how to adjust and use the server at

[http://localhost:<port><base-path>/index.html](http://localhost:2112/index.html)


Building
--------

    mvn clean install assembly:single
    cp target/wat-*-jar-with-dependencies.jar wat.jar

There is a Dockerfile that runs the demo project. After building the wat.jar, you can create the Dockerfile like this:

    cd docker
    ./build.sh

You can then start the demo-server like this:

    docker run -p <port>:2112 -t wat

Or just use the version in our [repository on docker hub](https://hub.docker.com/r/webis/wat/) like this:

    docker run -d -p 2112:2112 --restart=unless-stopped -t webis/wat:1.1.1
    
    
Citation
--------
If you use WAT-SL in your scientific work, please refer to it using [this publication](https://webis.de/publications.html#filter:WAT-SL%20A%20Customizable%20Web%20Annotation%20Tool%20for%20Segment%20Labeling):

Johannes Kiesel, Henning Wachsmuth, Khalid Al-Khatib, and Benno Stein. WAT-SL: A Customizable Web Annotation Tool for Segment Labeling. In Software Demonstrations at the 15th Conference of the European Chapter of the Association for Computational Linguistics (EACL 2017), pages 13-16, April 2017.
