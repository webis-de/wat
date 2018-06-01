WAT-SL
======
Web Annotation Tool for Segment Labeling.

Usage
-----
Unpack the example-project.zip and cd into the example project folder. Then use

    java -jar <path-to>/wat.jar [<port>]

to start the server on the given port (default port is 2112). You can then access documentation on how to adjust and use the server at

    http://localhost:<port>/index.html


Building
--------

    mvn clean install assembly:single
    cp target/wat-*-jar-with-dependencies.jar wat.jar 


