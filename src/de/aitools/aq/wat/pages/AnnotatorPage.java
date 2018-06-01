package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.util.List;

import de.aitools.aq.wat.WatServlet;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.TaskState;

public abstract class AnnotatorPage extends Page {

  protected static final String BUTTON_CLASS_TASK_SELECTION = "task-selection";
  
  public AnnotatorPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }
  
  public AnnotatorPage(final String projectName,
      final List<String> cssIncludes, final List<String> jsIncludes)
  throws NullPointerException {
    super(projectName, cssIncludes, jsIncludes);
  }

  public void print(final PrintWriter output,
      final Annotator annotator, final TaskState state,
      final boolean isAdminLogin) {
    output.append("<!DOCTYPE html>\n");
    output.append("<html>\n");

    this.startHead(output);
    this.printInFileScripts(output, annotator, state);
    output.append("</head>");
    this.printBody(output, annotator, state, isAdminLogin);
    output.append("</html>\n");
  }
  
  protected abstract void printInFileScripts(final PrintWriter output,
      final Annotator annotator, final TaskState state);
  
  protected void printBody(final PrintWriter output,
      final Annotator annotator, final TaskState state,
      final boolean isAdminLogin) {
    output.append("<body>\n");
    output.append("<form id=\"form\" action=\"")
      .append(WatServlet.ACTION_ANNOTATE)
      .append("\" method=\"POST\" accept-charset=\"utf-8\">\n");
    
    this.printHiddenElements(output, annotator, state, isAdminLogin);

    output.append("<section class=\"container\">");
    output.append("<div class=\"row col-xs-12 col-md-12\">");
    
    this.printTopNavbar(output, annotator, state, isAdminLogin);
    this.printMain(output, annotator, state);
    this.printBottomNavbar(output, annotator, state);

    output.append("</div>");
    output.append("</section>");
    output.append("</form>\n");
    output.append("</body>\n");
  }
  
  private void printHiddenElements(final PrintWriter output,
      final Annotator annotator, final TaskState state,
      final boolean isAdminLogin) {
    output.append("<input type=\"hidden\" name=\"")
      .append(WatServlet.REQUEST_PARAMETER_TIME_ZONE_OFFSET).append("\"/>");
    if (annotator != null) {
      output.append("<input id=\"task-internal-field-annotator\" type=\"hidden\" name=\"")
        .append(WatServlet.REQUEST_PARAMETER_ANNOTATOR).append("\" value=\"")
        .append(annotator.getLoginName()).append("\" />");
      output.append("<input id=\"task-internal-field-password\" type=\"hidden\" name=\"")
        .append(WatServlet.REQUEST_PARAMETER_PASSWORD).append("\" value=\"")
        .append(annotator.getPassword()).append("\" />");
      output.append("<input id=\"task-internal-field-is-admin-login\" type=\"hidden\" name=\"")
          .append(WatServlet.REQUEST_PARAMETER_ADMIN_LOGIN).append("\" value=\"")
          .append(String.valueOf(isAdminLogin)).append("\" />");
    }
    if (state != null) {
      output.append("<input id=\"task-internal-field-task\" type=\"hidden\" name=\"")
        .append(WatServlet.REQUEST_PARAMETER_TASK).append("\" value=\"")
        .append(state.getTask().getName()).append("\" />");
    }
  }
  
  private void printTopNavbar(final PrintWriter output,
      final Annotator annotator, final TaskState state,
      final boolean isAdminLogin) {
    output.append("<nav class=\"navbar navbar-default\">\n");
    output.append("  <div class=\"container-fluid\">\n");

    output.append("    <a class=\"navbar-brand\" href=\"#\">")
      .append(this.getProjectName()).append("</a>\n");

    if (annotator != null) {
      output.append("    <p class=\"navbar-text\">");
      output.append(annotator.getLoginName().replaceAll("\\s+", "&nbsp;"));
      if (state != null) {
        output.append("&commat;").append(
            state.getTask().getName().replaceAll("\\s+", "&nbsp;"));
      }
      if (isAdminLogin) {
        output.append(" (login through admin console)");
      }
      output.append("</p>\n");
    }

    output.append("  </div>\n");
    output.append("</nav>\n");
    
    if (annotator != null) {
      this.printBottomNavbar(output, annotator, state);
    }
  }
  
  protected abstract void printMain(final PrintWriter output,
      final Annotator annotator, final TaskState state);
  
  private void printBottomNavbar(final PrintWriter output,
      final Annotator annotator, final TaskState state) {
    if (annotator != null) {
      output.append("<nav class=\"navbar navbar-default\">\n");
      output.append("  <div class=\"container-fluid\">\n");
      this.printNavbarButtons(output, true, state != null);
      output.append("  </div>\n");
      output.append("</nav>\n");
    }
  }
  
  private void printNavbarButtons(final PrintWriter output,
      final boolean logout, final boolean taskSelection) {
    output.append("    <div class=\"btn-group\">\n");
    if (logout) {
      output.append("      <button type=\"button\" class=\"")
        .append(BUTTON_CLASS_LOGOUT)
        .append(" btn btn-default navbar-btn\">logout</button>\n");
    }
    if (taskSelection) {
      output.append("      <button type=\"button\" class=\"")
        .append(BUTTON_CLASS_TASK_SELECTION)
        .append(" btn btn-default navbar-btn\">task selection</button>\n");
    }
    output.append("    </div>");
  }

}
