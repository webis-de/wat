package de.aitools.aq.wat.pages;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map.Entry;

import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.Component;
import de.aitools.aq.wat.data.ComponentState;
import de.aitools.aq.wat.data.TaskState;

public class AnnotatorTaskPage extends AnnotatorPage {

	public AnnotatorTaskPage(final String projectName, final List<String> cssIncludes, final List<String> jsIncludes)
			throws NullPointerException {
		super(projectName, cssIncludes, Page.addToList(jsIncludes, "/js/wat-task.js"));
	}

	@Override
	protected void printInFileScripts(final PrintWriter output, final Annotator annotator, final TaskState state) {
		// Start
		output.append("<script>\n");
		output.append("$(document).ready(function() {\n");

		// Register value functions
		for (final Component component : state.getTask()) {
			output.append("  setValueFunctions[\"")
				.append(component.getName())
				.append("\"] = ")
				.append(component.getJsValueFunctionName())
				.append(";\n");
		}

		// Init functions
		for (final Component component : state.getTask())
		{
			for (final String jsInitFunction : component.getJsInitFunctionNames()) {
				output.append("  ")
					.append(jsInitFunction)
					.append("(\"")
					.append(component.getName())
					.append("\");\n");
			}
		}

		// Replay updates
		for (final Entry<String, ComponentState> componentEntry : state)
		{
			final String componentName = componentEntry.getKey();
			final ComponentState componentState = componentEntry.getValue();

			for (final Entry<String, String> entry : componentState)
			{
				final String value = entry.getValue().replaceAll("\n", "\\\\n").replaceAll("\"", "\\\\\"");
				
				output.append("  setValue(\"")
					.append(componentName)
					.append("\", \"")
					.append(entry.getKey())
					.append("\", \"")
					.append(value)
					.append("\");\n");
			}
		}

		// Set complete (or not)
		output.append("  setComplete(").append(String.valueOf(state.isComplete())).append(");\n");

		// End
		output.append("});\n");
		output.append("</script>\n");
	}

	@Override
	protected void printMain(final PrintWriter output, final Annotator annotator, final TaskState state) {
		output.append("<div class=\"panel\"><div class=\"panel-heading\">\n");
		output.append("  All progress is saved automatically.")
			.append(" You can return to the task selection at any time.\n");
		output.append("</div></div><br />\n");

		this.printTaskCompleteBox(output);
		try {
			for (final Component component : state.getTask()) {
				this.printComponent(output, component);
			}
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
		this.printTaskCompleteBox(output);
	}

	private void printComponent(final PrintWriter output, final Component component) throws IOException {
		output.append("<div class=\"panel panel-primary\">\n");
		output.append("  <div id=\"")
			.append(component.getName())
			.append("-toggle\"")
			.append(" class=\"panel-heading panel-collapse-toggle collapsed\"")
			.append(" data-toggle=\"collapse\"")
			.append(" data-target=\"#")
			.append(component.getName())
			.append("-body\">\n");
		output.append("    <strong>");
		output.append(component.getTitle());
		output.append("</strong> (");
		
		if (!component.isRequired()) {
			output.append("optional");
		}
		else
		{
			output.append("<span id=\"").append(component.getName()).append("-state\"></span>");
		}
		
		output.append(")</div>\n");
		output.append("  <div id=\"")
			.append(component.getName())
			.append("-body\"")
			.append(" class=\"panel-collapse collapse\">");
		
		output.append("<div class=\"panel-body\">\n");
		
		if (component.getDescription() != null) {
			output.append("    <div class=\"subtask-description\">");
			output.append(component.getDescription());
			output.append("</div>\n");
		}

		component.printHtmlInPanel(output);

		output.append("  </div></div>\n");
		output.append("</div>\n");
	}

	private void printTaskCompleteBox(final PrintWriter output) {
		output.append("<div class=\"is-complete-box panel panel-success\"").append(" style=\"display:none;\">")
				.append("<div class=\"panel-heading\"><strong>\n");
		output.append("  This task is completed.").append(" You may do further changes if you want.\n");
		output.append("</strong></div></div>\n");
	}

}
