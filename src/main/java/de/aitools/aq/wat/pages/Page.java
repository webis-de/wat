package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Page {

  protected static final String BUTTON_CLASS_LOGOUT = "button-logout";
  
  private final String projectName;
  
  private final List<String> cssIncludes;
  
  private final List<String> jsIncludes;
  
  public Page(final String projectName)
  throws NullPointerException {
    this(projectName, Collections.emptyList(), Collections.emptyList());
  }
  
  public Page(final String projectName,
      final List<String> cssIncludes, final List<String> jsIncludes)
  throws NullPointerException {
    if (projectName == null) {
      throw new NullPointerException("The server name must not be null");
    }
    if (cssIncludes == null) {
      throw new NullPointerException("The cssIncludes must not be null");
    }
    if (jsIncludes == null) {
      throw new NullPointerException("The jsIncludes must not be null");
    }
    this.projectName = projectName;
    this.cssIncludes = cssIncludes;
    this.jsIncludes = jsIncludes;
  }
  
  protected static List<String> addToList(
      final List<String> list, final String... elements) {
    if (list == null) {
      return Page.addToList(new ArrayList<>(elements.length), elements);
    } else {
      for (final String element : elements) {
        list.add(element);
      }
      return list;
    }
  }
  
  protected String getProjectName() {
    return this.projectName;
  }
  
  protected void startHead(final PrintWriter output) {
    output.append("<head>\n");
    output.append("<title>").append(this.getProjectName()).append("</title>\n");
    output.append("<link href=\"../css/bootstrap.min.css\" rel=\"stylesheet\" />\n");
    output.append("<link href=\"../css/wat.css\" rel=\"stylesheet\" />\n");
    for (final String cssInclude : this.cssIncludes) {
      output.append("<link href=\"..").append(cssInclude)
        .append("\" rel=\"stylesheet\" />\n");
    }
    output.append("<script src=\"../js/jquery.min.js\"></script>\n");
    output.append("<script src=\"../js/bootstrap.min.js\"></script>\n");
    output.append("<script src=\"../js/wat.js\"></script>\n");
    for (final String jsInclude : this.jsIncludes) {
      output.append("<script src=\"..").append(jsInclude)
        .append("\"></script>\n");
    }
  }

}
