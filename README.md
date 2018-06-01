WAT-SL
======
Web Annotation Tool for Segment Labeling.

Demo server
-----------
For a quick look into WAT-SL, please visit [our demo server](https://webis16.medien.uni-weimar.de/wat-sl/).


Usage
-----
Download the [wat.jar](https://github.com/webis-de/wat/releases/download/1.1.1/wat.jar). Then unpack the [example-project.zip](https://github.com/webis-de/wat/releases/download/1.1.0/example-project.zip) and cd into the example project folder. Then use

    java -jar <path-to>/wat.jar [<port>]

to start the server on the given port (default port is 2112). You can then access documentation on how to adjust and use the server at

    [http://localhost:<port>/index.html](http://localhost:2112/index.html)


Building
--------

    mvn clean install assembly:single
    cp target/wat-*-jar-with-dependencies.jar wat.jar 


