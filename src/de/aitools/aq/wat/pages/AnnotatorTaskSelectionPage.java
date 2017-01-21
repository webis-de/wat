package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Map.Entry;

import de.aitools.aq.wat.WatServlet;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;

public class AnnotatorTaskSelectionPage extends AnnotatorPage {

  private static final DateFormat DATE_FORMAT =
      AnnotatorTaskSelectionPage.compileDateFormat("yyyy-MM-dd HH:mm");
  
  private static DateFormat compileDateFormat(final String format) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat;
  }

  private static final String FIRST_UNCOMPLETED_TASK_ID = "first-uncompleted";

  public AnnotatorTaskSelectionPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }

  @Override
  protected void printInFileScripts(final PrintWriter output,
      final Annotator annotator, final TaskState state) {
    output.append("<script>\n");
    output.append("$(document).ready(function() {\n");
    output.append(  "var first = document.getElementById(\""
        + FIRST_UNCOMPLETED_TASK_ID + "\");");
    output.append("  if (first != null) { first.scrollIntoView(); }\n");

    output.append("});\n");
    output.append("</script>\n");
  }

  @Override
  protected void printMain(final PrintWriter output,
      final Annotator annotator, final TaskState state) {
    output.append("<div class=\"panel\"><div class=\"panel-heading\">\n");
    output.append("  Choose a task below in order to work on it.")
      .append(" All progress is saved automatically.")
      .append(" You can return to this page at any time.\n");
    output.append("</div></div><br />\n");

    final int numTasksCompleted = this.printProgressPanel(output, annotator);
    this.printTaskSelectionTable(output, annotator, numTasksCompleted);
  }
  
  private int printProgressPanel(final PrintWriter output,
      final Annotator annotator) {
    int numTasksCompleted = 0;
    int numTasks = 0;
    for (final Entry<Task, TaskState> entry : annotator) {
      ++numTasks;
      if (entry.getValue().isComplete()) { ++numTasksCompleted; }
    }
    output.append("<div class=\"panel");
    if (numTasksCompleted == numTasks) {
      output.append(" panel-success");
    } else {
      output.append(" panel-default");
    }
    output.append("\"><div class=\"panel-heading\">\n");
    output.append("  You have completed ");
    output.append(String.valueOf(numTasksCompleted));
    output.append(" of ");
    output.append(String.valueOf(numTasks));
    output.append(" tasks.\n");
    output.append("</div></div><br />\n");
    return numTasksCompleted;
  }
  
  private void printTaskSelectionTable(final PrintWriter output,
      final Annotator annotator, final int numTasksCompleted) {
    output.append("<table class=\"table taskselection\">\n");
    output.append("  <thead><tr>\n");
    output.append("    <th>Task</th>\n");
    output.append("    <th>Progress</th>\n");
    output.append("    <th>Last update</th>\n");
    output.append("  </tr></thead>\n");
    output.append("  <tbody>\n");

    boolean allCompletedSoFar = true;
    for (final Entry<Task, TaskState> entry : annotator) {
      final Task task = entry.getKey();
      final TaskState state = entry.getValue();

      final String name = task.getName();
      final Date lastUpdate = state.getLastUpdateInLocalTime();
      final String lastUpdateString =
          lastUpdate == null ? "-" : DATE_FORMAT.format(lastUpdate);
      final boolean isComplete = state.isComplete();
      if (isComplete) {
        output.append("    <tr class=\"success\">\n");
      } else {
        if (allCompletedSoFar && numTasksCompleted > 0) {
          output.append("    <tr id=\"" + FIRST_UNCOMPLETED_TASK_ID + "\">\n");
          allCompletedSoFar = false;
        } else {
          output.append("    <tr>\n");
        }
      }

      output.append("      <td>")
        .append("<button type=\"submit\" class=\"btn btn-default\" name=\"")
        .append(WatServlet.REQUEST_PARAMETER_TASK).append("\" value=\"")
        .append(name).append("\">").append(name).append("</button></td>\n");
      output.append("      <td>").append(state.getProgress()).append("</td>\n");
      output.append("      <td>").append(lastUpdateString).append("</td>\n");

      output.append("    </tr>\n");
    }
    output.append("  </tbody>\n");
    output.append("</table>\n");
  }

}
