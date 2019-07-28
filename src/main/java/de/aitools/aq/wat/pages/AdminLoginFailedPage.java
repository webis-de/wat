package de.aitools.aq.wat.pages;

import java.io.PrintWriter;

public class AdminLoginFailedPage extends AdminLoginPage {

  public AdminLoginFailedPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }

  @Override
  protected void printMain(final PrintWriter output) {
    output.append("<div class=\"panel panel-danger\">")
      .append("<div class=\"panel-heading\">\n");
    output.append("  Authentication failed: Username or password wrong\n");
    output.append("</div></div><br />\n");
    
    super.printMain(output);
  }

}
