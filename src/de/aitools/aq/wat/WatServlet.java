package de.aitools.aq.wat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;
import de.aitools.aq.wat.pages.AdminControlsPage;
import de.aitools.aq.wat.pages.AdminLoginFailedPage;
import de.aitools.aq.wat.pages.AdminLoginPage;
import de.aitools.aq.wat.pages.AdminPage;
import de.aitools.aq.wat.pages.AnnotatorLoginFailedPage;
import de.aitools.aq.wat.pages.AnnotatorLoginPage;
import de.aitools.aq.wat.pages.AnnotatorPage;
import de.aitools.aq.wat.pages.AnnotatorTaskSelectionPage;

public class WatServlet extends HttpServlet {

  private static final long serialVersionUID = 4526811543827008234L;

  public static final String ACTION_ANNOTATE = "annotate";

  public static final String ACTION_ADMIN = "admin";

  public static final String ACTION_UPDATE_STATE = "update";

  public static final String REQUEST_PARAMETER_ANNOTATOR = "annotator";

  public static final String REQUEST_PARAMETER_PASSWORD = "password";

  public static final String REQUEST_PARAMETER_TASK = "task";

  public static final String REQUEST_PARAMETER_COMPONENT = "component";

  public static final String REQUEST_PARAMETER_KEY = "key";

  public static final String REQUEST_PARAMETER_VALUE = "value";

  public static final String REQUEST_PARAMETER_TIME_ZONE_OFFSET = "timezone";

  public static final String REQUEST_PARAMETER_ADMIN_ACTION = "admin-action";
  
  public static final String ADMIN_ACTION_RELOAD = "Reload server";
  
  public static final String ADMIN_ACTION_RESULTS = "Write results";
  
  private final WatProject project;
  
  private final AnnotatorPage annotatorLoginPage;
  
  private final AnnotatorPage annotatorLoginFailedPage;
  
  private final AnnotatorPage annotatorTaskSelectionPage;
  
  private final AdminPage adminLoginPage;
  
  private final AdminPage adminLoginFailedPage;
  
  private final AdminPage adminControlsPage;
  
  public WatServlet() throws IOException {
    this.project = new WatProject(new File("."));
    final String projectName = this.project.getName();
    this.annotatorLoginPage = new AnnotatorLoginPage(projectName);
    this.annotatorLoginFailedPage = new AnnotatorLoginFailedPage(projectName);
    this.annotatorTaskSelectionPage = new AnnotatorTaskSelectionPage(projectName);
    this.adminLoginPage = new AdminLoginPage(projectName);
    this.adminLoginFailedPage = new AdminLoginFailedPage(projectName);
    this.adminControlsPage = new AdminControlsPage(projectName);
  }

  @Override
  protected void doGet(
      final HttpServletRequest request, final HttpServletResponse response)
  throws ServletException, IOException {
    this.serve(request, response);
  }

  @Override
  protected void doPost(
      final HttpServletRequest request, final HttpServletResponse response)
  throws ServletException, IOException {
    this.serve(request, response);
  }

  private void serve(
      final HttpServletRequest request, final HttpServletResponse response)
  throws ServletException, IOException {
    try (final PrintWriter output = response.getWriter()) {
      final String action = this.getAction(request);
      switch (action) {
      case ACTION_ANNOTATE:
        response.setContentType("text/html");
        this.serveAnnotate(request, output);
        break;
      case ACTION_UPDATE_STATE:
        response.setContentType("application/json");
        this.serveUpdateState(request, output);
        break;
      case ACTION_ADMIN:
        response.setContentType("text/html");
        this.serveAdmin(request, output);
        break;
      default:
        response.setContentType("text/html");
        output.append(
            "<html><head><title>Error</title></head><body>No such action: ")
          .append(action).append("</body></html>");
      }
    }
  }
  
  private String getAction(final HttpServletRequest request) {
    final String requestUri = request.getRequestURI();
    assert requestUri.startsWith(WatServletServer.SERVLET_PATH);
    // + 1 due to the "/" following the path
    final int requestOffset = WatServletServer.SERVLET_PATH.length() + 1;
    if (requestOffset >= requestUri.length()) {
      return ACTION_ANNOTATE;
    } else {
      final String[] requestParts =
          requestUri.substring(requestOffset).split("/");
      return requestParts[0];
    }
  }

  private void serveAnnotate(
      final HttpServletRequest request, final PrintWriter output)
  throws IOException {
    final String annotatorName =
        request.getParameter(REQUEST_PARAMETER_ANNOTATOR);
    final String password =
        request.getParameter(REQUEST_PARAMETER_PASSWORD);
    final Annotator annotator = this.project.getAnnotator(annotatorName);
    if (annotatorName == null || annotatorName.isEmpty()) {
      this.annotatorLoginPage.print(output, null, null);
    } else if (annotator == null || !annotator.checkPassword(password)) {
      this.annotatorLoginFailedPage.print(output, null, null);
    } else {
      final String taskName =
          request.getParameter(REQUEST_PARAMETER_TASK);
      final Task task = annotator.getTask(taskName);
      if (task == null) {
        this.annotatorTaskSelectionPage.print(output, annotator, null);
      } else {
        final int timeZoneOffset = Integer.parseInt(
            request.getParameter(REQUEST_PARAMETER_TIME_ZONE_OFFSET));
        final String client = this.getClient(request);
        final TaskState state = annotator.getState(task);
        state.workerOpenedTask(client, timeZoneOffset);
        task.getPage().print(output, annotator, state);
      }
    }
  }

  private void serveUpdateState(
      final HttpServletRequest request, final PrintWriter output)
  throws IllegalArgumentException, IOException {
    final String annotatorName =
        request.getParameter(REQUEST_PARAMETER_ANNOTATOR);
    final String password =
        request.getParameter(REQUEST_PARAMETER_PASSWORD);
    final Annotator annotator = this.project.getAnnotator(annotatorName);
    if (annotator != null && annotator.checkPassword(password)) {
      final String taskName =
          request.getParameter(REQUEST_PARAMETER_TASK);
      final Task task = annotator.getTask(taskName);
      if (task != null) {
        final String client = this.getClient(request);
        final int timeZoneOffset = Integer.parseInt(
            request.getParameter(REQUEST_PARAMETER_TIME_ZONE_OFFSET));
        final TaskState state = annotator.getState(task);
        if (state != null) {
          final String componentName =
              request.getParameter(REQUEST_PARAMETER_COMPONENT);
          final String key =
              request.getParameter(REQUEST_PARAMETER_KEY);
          final String value =
              request.getParameter(REQUEST_PARAMETER_VALUE);
          if (componentName != null && key != null && value != null) {
            state.setValue(componentName, key, value, client, timeZoneOffset);
            final boolean isComplete = state.isComplete();
            output.append("{\"success\":true,\"complete\":"+isComplete+"}");
            return;
          }
        }
      }
    }
  }

  private void serveAdmin(
      final HttpServletRequest request, final PrintWriter output)
  throws IOException {
    final String password =
        request.getParameter(REQUEST_PARAMETER_PASSWORD);
    if (password == null || password.isEmpty()) {
      this.adminLoginPage.print(output, null);
    } else if (!this.project.getAdminPassword().equals(password)) {
      this.adminLoginFailedPage.print(output, null);
    } else {
      final String adminAction =
          request.getParameter(REQUEST_PARAMETER_ADMIN_ACTION);
      boolean adminActionSuccess = true;
      if (adminAction != null) {
        switch (adminAction) {
        case ADMIN_ACTION_RELOAD:
          this.project.reload();
          if (!this.project.getAdminPassword().equals(password)) {
            this.adminLoginFailedPage.print(output, null);
            return;
          }
          break;
        case ADMIN_ACTION_RESULTS:
          this.project.writeResults();
          break;
        default:
          adminActionSuccess = false;
          break;
        }
      }
      this.adminControlsPage.print(
          output, password, adminAction, adminActionSuccess);
    }
  }
  
  private String getClient(final HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");  
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
        ip = request.getHeader("Proxy-Client-IP");  
    }  
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
        ip = request.getHeader("WL-Proxy-Client-IP");  
    }  
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
        ip = request.getHeader("HTTP_CLIENT_IP");  
    }  
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
    }  
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
        ip = request.getRemoteAddr();  
    }  
    return ip; 
  }

}
