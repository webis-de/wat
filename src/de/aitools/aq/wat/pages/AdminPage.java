package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.util.List;

import de.aitools.aq.wat.WatServlet;

public abstract class AdminPage extends Page {
  
  public AdminPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }
  
  public void print(final PrintWriter output,
      final String password) {
    this.print(output, password, null, false, null);
  }
  
  public void print(final PrintWriter output, final String password,
      final String adminAction, final boolean adminActionSuccess,
      final List<Panel> panels) {
    output.append("<!DOCTYPE html>\n");
    output.append("<html>\n");
    this.startHead(output);
    output.append("</head>");
    this.printBody(output, password, adminAction, adminActionSuccess, panels);
    output.append("</html>\n");
  }
  
  private void printBody(final PrintWriter output, final String password,
      final String adminAction, final boolean adminActionSuccess,
      final List<Panel> panels) {
    output.append("<body>\n");
    output.append("<form id=\"form\" action=\"")
      .append(WatServlet.ACTION_ADMIN)
      .append("\" method=\"POST\" accept-charset=\"utf-8\">\n");
    
    this.printHiddenElements(output, password);

    output.append("<section class=\"container\" id=\"Other\" style=\"margin-bottom:15px; padding: 10px 10px; font-family: Verdana, Geneva, sans-serif; color:#333333; font-size:0.9em;\">");
    output.append("<div class=\"row col-xs-12 col-md-12\">");
    
    this.printTopNavbar(output, password != null);
    if (adminAction != null) {
      if (adminActionSuccess) {
        this.printActionSuccessBox(output, adminAction);
      } else {
        this.printActionFailedBox(output, adminAction);
      }
    }
    this.printMain(output);
    this.printBottomNavbar(output, password != null);
    output.append("</div>");
    output.append("</section>");
    output.append("</form>\n");

    output.append("<section class=\"container\">");
    output.append("<div class=\"row col-xs-12 col-md-12\">");
    this.printPanels(output, panels);
    output.append("</div>");
    output.append("</section>");
    
    output.append("</body>\n");
  }
  
  private void printHiddenElements(
      final PrintWriter output, final String password) {
    if (password != null) {
      output.append("<input id=\"task-internal-field-password\" type=\"hidden\" name=\"")
        .append(WatServlet.REQUEST_PARAMETER_PASSWORD).append("\" value=\"")
        .append(password).append("\" />");
      output.append("<input type=\"hidden\" name=\"")
        .append(WatServlet.REQUEST_PARAMETER_TIME_ZONE_OFFSET).append("\"/>");
    }
  }
  
  private void printActionSuccessBox(
      final PrintWriter output, final String adminAction) {
    output.append("<div class=\"panel panel-success\">")
      .append("<div class=\"panel-heading\"><strong>\n");
    output.append("  Completed: ").append(adminAction).append("\n");
    output.append("</strong></div></div>\n");
  }
  
  private void printActionFailedBox(
      final PrintWriter output, final String adminAction) {
    output.append("<div class=\"panel panel-danger\">")
      .append("<div class=\"panel-heading\"><strong>\n");
    output.append("  Failed: ").append(adminAction).append("\n");
    output.append("</strong></div></div>\n");
  }
  
  private void printTopNavbar(
      final PrintWriter output, final boolean loggedIn) {
    output.append("<nav class=\"navbar navbar-default\">\n");
    output.append("  <div class=\"container-fluid\">\n");

    output.append("    <a class=\"navbar-brand\" href=\"#\">")
      .append(this.getProjectName()).append("</a>\n");

    output.append("  </div>\n");
    output.append("</nav>\n");
    
    this.printBottomNavbar(output, loggedIn);
  }
  
  protected abstract void printMain(
      final PrintWriter output);

  protected void printPanels(
      final PrintWriter output, final List<Panel> panels) {
    if (panels != null) {
      for (final Panel panel : panels) {
        panel.printPanel(output);
        output.append("<br/>\n\n");
      }
    }
  }
  
  private void printBottomNavbar(
      final PrintWriter output, final boolean loggedIn) {
    if (loggedIn) {
      output.append("<nav class=\"navbar navbar-default\">\n");
      output.append("  <div class=\"container-fluid\">\n");
      this.printNavbarButtons(output, true);
      output.append("  </div>\n");
      output.append("</nav>\n");
    }
  }
  
  private void printNavbarButtons(
      final PrintWriter output, final boolean logout) {
    output.append("    <div class=\"btn-group\">\n");
    if (logout) {
      output.append("      <button type=\"button\" class=\"")
        .append(BUTTON_CLASS_LOGOUT)
        .append(" btn btn-default navbar-btn\">logout</button>\n");
    }
    output.append("    </div>");
  }

}
