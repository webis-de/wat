package de.aitools.aq.wat.pages;

import java.io.PrintWriter;

import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.TaskState;

public class AnnotatorLoginFailedPage extends AnnotatorLoginPage {

  public AnnotatorLoginFailedPage(final String projectName)
  throws NullPointerException {
    super(projectName);
  }

  @Override
  protected void printMain(final PrintWriter output,
      final Annotator annotator, final TaskState state) {
    output.append("<div class=\"panel panel-danger\">")
      .append("<div class=\"panel-heading\">\n");
    output.append("  Authentication failed: Username or password wrong\n");
    output.append("</div></div><br />\n");
    
    super.printMain(output, annotator, state);
  }

}
