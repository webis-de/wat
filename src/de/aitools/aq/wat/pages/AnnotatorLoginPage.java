package de.aitools.aq.wat.pages;

import java.io.PrintWriter;

import de.aitools.aq.wat.WatServlet;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.TaskState;

public class AnnotatorLoginPage extends AnnotatorPage {

  public AnnotatorLoginPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }

  @Override
  protected void printInFileScripts(final PrintWriter output,
      final Annotator annotator, final TaskState state) {
    // Do nothing
  }

  @Override
  protected void printMain(final PrintWriter output,
      final Annotator annotator, final TaskState state) {
    output.append("<div class=\"input-group login\">\n");
    output.append("  <span class=\"input-group-addon\">Username: </span>\n");
    output.append("  <input class=\"form-control\" type=\"text\" name=\"")
      .append(WatServlet.REQUEST_PARAMETER_ANNOTATOR).append("\" />\n");
    output.append("</div>\n");

    output.append("<div class=\"input-group login\">\n");
    output.append("  <span class=\"input-group-addon\">Password: </span>\n");
    output.append("  <input class=\"form-control\" type=\"password\" name=\"")
      .append(WatServlet.REQUEST_PARAMETER_PASSWORD).append("\" />\n");
    output.append("</div>\n");

    output.append("<br />\n");
    output.append("<button type=\"submit\" name=\"Login\"")
      .append(" class=\"btn btn-default\">Login</button>\n");
  }

}
