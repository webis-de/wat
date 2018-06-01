package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.util.Map.Entry;

import de.aitools.aq.wat.WatProject;
import de.aitools.aq.wat.WatServlet;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;
import de.aitools.aq.wat.io.Client;

public class AdminAnnotationProgressPanel extends Panel {

  private static final String NAME = "Annotation Progress";

  private final WatProject project;

  public AdminAnnotationProgressPanel(final WatProject project)  {
    super(NAME);
    if (project == null) {
      throw new NullPointerException();
    }
    this.project = project;
  }

  @Override
  protected void printPanelContent(final PrintWriter output) {
    output.println("<table class='table'>");
    output.println("<thead>");
    output.print("<tr>");
    output.print("<th scope='col'>Annotator</th>");
    output.print("<th scope='col' colspan='2' class='center'>Tasks</th>");
    output.print("<th scope='col' class='center'>Time</th>");
    output.print("<th scope='col'>Login (new window)</th>");
    output.println("</tr>");
    output.println("</tbead>");
    output.println("<tbody>");

    for (final Annotator annotator : this.project) {
      int numTasks = 0;
      int numTasksCompleted = 0;
      int workTimeInSeconds = 0;
      for (final Entry<Task, TaskState> taskEntry : annotator) {
        final TaskState taskState = taskEntry.getValue();
        ++numTasks;
        if (taskState.isComplete()) {
          ++numTasksCompleted;
        }
        workTimeInSeconds += taskState.getAnnotatorWorkTimeInSeconds();
      }

      output.print("<tr");
      if (numTasksCompleted == numTasks)
      {
        output.print(" class='success'");
      }
      output.print(">");

      this.printAnnotatorName(output, annotator);
      this.printAnnotatorProgress(output, numTasks, numTasksCompleted, annotator);
      this.printAnnotatorWorkTime(output, workTimeInSeconds);
      this.printLoginButton(output, annotator);
      output.println("</tr>");
    }

    output.println("</tbody>");
    output.println("</table>");
    
    
    output.println("When logged in using the buttons above, record files "
        + "will show '" + Client.ADMIN + "' instead of '" + Client.ANNOTATOR
        + "' and all actions performed will be ignored when calculating the "
        + "time spent.");
  }

  protected void printAnnotatorName(final PrintWriter output, final Annotator annotator) {
    output.print("<th scope='row'>");
    output.print(annotator.getLoginName() + " (" + annotator.getName() + ")");
    output.print("</th>");
  }

  protected void printAnnotatorProgress(final PrintWriter output,
      final int numTasks, final int numTasksCompleted,
      final Annotator annotator) {
    output.print("<td class='right'>");
    output.print(String.valueOf(numTasksCompleted));
    output.print("/");
    output.print(String.valueOf(numTasks));
    output.print("</td>");

    output.print("<td class='right'>");
    double ratio = ((double) numTasksCompleted) / numTasks;
    output.print("" + String.valueOf(Math.round(ratio * 100)) + "%");
    output.print("</td>");
    
  }

  protected void printAnnotatorWorkTime(
      final PrintWriter output, final int workTimeInSeconds) {
    output.print("<td class='center'>");
    
    int timeInMinutes = workTimeInSeconds / 60;
    
    if (workTimeInSeconds != 0) {
      int timeInHours = timeInMinutes / 60;
      int timeMin = timeInMinutes % 60;
      int timeSec = workTimeInSeconds % 60;
      
      output.print(String.valueOf(timeInHours));
      
      output.print(":");
      
      if (String.valueOf(timeMin).length() == 1) {
        output.print("0");
      }
      
      output.print(String.valueOf(timeMin) + ":");
      
      if (String.valueOf(timeSec).length() == 1) {
        output.print("0");
      }
      
      output.print(timeSec);
    } else {
      output.print("--");
    }
    
    output.print("</td>");
  }

  protected void printLoginButton(
      final PrintWriter output, final Annotator annotator) {
    output.print("<td>");
    output.print("<form action='annotate' method='POST' target='_blank'>");
    output.print("<button type='submit' class='btn btn-default'>");
    output.print("Login as ");
    output.print(annotator.getLoginName());
    output.print("</button>");

    output.print(
        "<input type='hidden' name='"
        + WatServlet.REQUEST_PARAMETER_ANNOTATOR + "' value='"
        + annotator.getLoginName() + "' />");
    
    output.print(
        "<input type='hidden' name='"
        + WatServlet.REQUEST_PARAMETER_PASSWORD + "' value='"
        + annotator.getPassword() + "' />");
    
    output.print("<input type='hidden' name='"
        + WatServlet.REQUEST_PARAMETER_ADMIN_LOGIN
        + "' value='true' />");

    output.print("</form>");
    output.print("</td>");
  }
}