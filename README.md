WAT-SL
======
Web Annotation Tool for Segment Labeling.

Usage
-----
Unpack the example-project.zip and cd into the example project folder. Then use

    java -jar ../wat.jar [<port>]

to start the server on the given port (default port is 2112). You can then access documentation on how to adjust and use the server at

    http://localhost:<port>/index.html


Building
--------
Building the JAR anew requires the following libraries:
  - Jetty 9.4.0
  - Apache commons lang3 3.1

Launch configuration: de.aitools.aq.wat.WatServletServer

