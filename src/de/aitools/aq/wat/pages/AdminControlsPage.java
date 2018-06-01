package de.aitools.aq.wat.pages;

import java.io.PrintWriter;

import de.aitools.aq.wat.WatServlet;

public class AdminControlsPage extends AdminPage {

  public AdminControlsPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }

  @Override
  protected void printMain(final PrintWriter output) {
    this.makeButton(output, WatServlet.ADMIN_ACTION_RELOAD,
        "Updates all configurations and reads again all tasks");
    this.makeButton(output, WatServlet.ADMIN_ACTION_RESULTS,
        "Writes the 'key = value' mappings of all completed tasks to the "
        + "'results' directory in the server directory.");
    
    output.append("<div class=\"panel\">\n");
    this.makeButton(output, WatServlet.ADMIN_ACTION_SHOW_PROGRESS, null);
    output.append("</div>\n");

    output.append("<br/>\n");
  }
  
  private void makeButton(
      final PrintWriter output, final String value, final String text) {
    output.append("<div class=\"panel\">\n");
    output.append("  <button type=\"submit\" name=\"")
      .append(WatServlet.REQUEST_PARAMETER_ADMIN_ACTION).append("\" value=\"")
      .append(value).append("\" class=\"btn btn-default\">");
    output.append(value);
    output.append("</button>\n");
    if (text != null) {
      output.append("  <br/>").append(text).append('\n');
    }
    output.append("</div>\n");
  }

}
