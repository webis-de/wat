package de.aitools.aq.wat;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.ResourceCollection;

public class WatServletServer {
  
  public static final String SERVLET_PATH = "/wat";
  
  private static final int DEFAULT_PORT = 2112;

  private static final String DEFAULT_BASE_PATH = "/";
  
  private static final String EMBEDDED_RESOURCES_DIR = "web";
  
  private static final String LOCAL_RESOURCES_DIR = "web";
  
  public static void main(final String[] args) throws Exception {
    int port = DEFAULT_PORT;
    String basePath = DEFAULT_BASE_PATH;
    if (args.length > 0) { port = Integer.parseInt(args[0]); }
    if (args.length > 1) { basePath = args[1]; }
    System.out.println("Starting WAT on port " + port);
    final Server server = new Server(port);

    final ServletContextHandler context = new ServletContextHandler();
    context.setContextPath(basePath);
    context.setBaseResource(WatServletServer.createBaseResource());
    server.setHandler(context);
    
    final ServletHolder annotationServlet =
        new ServletHolder("wat", WatServlet.class);
    context.addServlet(annotationServlet, SERVLET_PATH + "/*");
    
    final ServletHolder resourcesServlet =
        new ServletHolder("static-embedded", DefaultServlet.class);
    resourcesServlet.setInitParameter("dirAllowed", "true");
    context.addServlet(resourcesServlet, "/");
    
    server.start();
    System.out.println("Access the server at: http://localhost:" + port + basePath);
    if (basePath.equals("/")) {
      System.out.println("Direct annotators to: http://localhost:" + port + SERVLET_PATH);
    } else {
      System.out.println("Direct annotators to: http://localhost:" + port + basePath + SERVLET_PATH);
    }
    server.join();
  }
  
  private static ResourceCollection createBaseResource()
  throws Exception {
    final String embeddedBase = WatServletServer.class.getClassLoader()
        .getResource(EMBEDDED_RESOURCES_DIR).toExternalForm();
    String[] bases = new String[] {embeddedBase};
    
    final File localBaseFile =
        new File(new File(System.getProperty("user.dir")), LOCAL_RESOURCES_DIR);
    if (localBaseFile.isDirectory()) {
      final String localBase = localBaseFile.toURI().toURL().toExternalForm();
      bases = new String[] {localBase, embeddedBase};
    }

    return new ResourceCollection(bases);
  }

}
