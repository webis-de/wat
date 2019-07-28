package de.aitools.aq.wat.pages;

import java.io.PrintWriter;

import de.aitools.aq.wat.WatServlet;

public class AdminLoginPage extends AdminPage {

  public AdminLoginPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }

  @Override
  protected void printMain(final PrintWriter output) {
    output.append("<div class=\"input-group login\">\n");
    output.append("  <span class=\"input-group-addon\">Admin </span>\n");
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
