package de.aitools.aq.wat.components;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;

import de.aitools.aq.wat.data.Component;
import de.aitools.aq.wat.data.ComponentState;
import de.aitools.aq.wat.io.RecordsFile;

public class TextBox extends Component {
  
  private static final String JS_VALUE_FUNCTION_NAME = "setComponentValue";
  
  private static final String KEY = "text";
  
  private Unit unit;

  public TextBox(
      final String name,
      final File projectDirectory, final File taskDirectory, 
      final Map<String, RecordsFile> recordFilesByAnnotator)
  throws IOException {
    super(name, projectDirectory, taskDirectory, recordFilesByAnnotator,
        JS_VALUE_FUNCTION_NAME);
    this.addJsIncludes("/js/set-component-value.js");
    this.setUseMajorityAnnotation(false);
  }

  @Override
  public void printHtmlInPanel(final PrintWriter output) {
    output.append("<textarea class=\"form-control\" rows=\"5\" id=\"")
      .append(this.getName()).append('-').append(KEY)
      .append("\" onblur=\"javascript:update('").append(this.getName())
      .append("', '").append(KEY).append("', $('#")
      .append(this.getName()).append('-').append(KEY)
      .append("').val())\"></textarea>");

    // Write for curation
    for (final String text : this.unit.getLabels()) {
      for (final String annotator : this.unit.getAnnotators(text)) {
        output.append(annotator);
        output.append(":");
        output.append("<textarea class=\"form-control\" rows=\"5\"")
          .append("disabled=\"disabled\">");
        output.append(StringEscapeUtils.escapeHtml4(text));
        output.append("</textarea>");
      }
    }
  }

  @Override
  protected void loadForTask(final Properties config,
      final File projectDirectory, final File taskDirectory)
  throws IOException {
    this.unit = new Unit(KEY);
    this.addUnit(this.unit);
  }

  @Override
  protected ComponentState createNewStateInstance() {
    return new TextBoxState();
  }
  
  private class TextBoxState extends ComponentState {
    
    private String value;
    
    public TextBoxState() {
      this.value = "";
    }

    @Override
    public boolean isComplete() {
      return !this.value.isEmpty();
    }

    @Override
    protected void updateProgress(final String key, final String value) {
      this.value = value;
    }
    
  }

}
