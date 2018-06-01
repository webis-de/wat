package de.aitools.aq.wat.pages;

import java.io.PrintWriter;

public abstract class Panel {
  
  private final String name;
  
  public Panel(final String name) {
    if (name == null) { throw new NullPointerException(); }
    this.name = name;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void printPanel(final PrintWriter output) {
    output.append("<div class=\"panel panel-primary\">\n");
    output.append("  <div class=\"panel-heading\">\n");
    output.append("    <strong>").append(this.getName())
      .append("</strong>\n");
    output.append("  </div>\n");
    output.append("  <div class=\"panel-body\">\n");
    this.printPanelContent(output);
    output.append("  </div>\n");
    output.append("</div>\n");
  }
  
  protected abstract void printPanelContent(final PrintWriter output);

}
