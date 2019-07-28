package de.aitools.aq.wat.pages;

// TODO: Count of Sections!

import java.io.PrintWriter;
import java.util.Map.Entry;

import de.aitools.aq.wat.WatProject;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.ComponentState;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;

public class AdminAnnotationDetailedProgressPanel extends Panel
{

	private static final String NAME = "Detailed Annotation Progress";

	private final WatProject project;

	public AdminAnnotationDetailedProgressPanel(WatProject project)
	{
		super(NAME);

		if (project == null)
		{
			throw new NullPointerException();
		}

		this.project = project;
	}

	@Override
	protected void printPanelContent(final PrintWriter output)
	{
		for (final Annotator annotator : this.project)
		{
			final String annotatorName = annotator.getLoginName() + " (" + annotator.getName() + ")";

			output.println("<h2>Annotator: <b>" + annotatorName + "</b></h2>");

			output.println("<table class='table'>");
			output.println("<thead>");
			output.print("<tr>");
			output.print("<th scope='col'>Task</th>");
			output.print("<th scope='col' colspan='2' class='center'>Segments</th>");
			output.print("<th scope='col' class='center'>Time</th>");
			output.println("</tr>");
			output.println("</tbead>");
			output.println("<tbody>");

			for (final Entry<Task, TaskState> task : annotator)
			{
				final String taskName = task.getKey().getName();
				
				for (final Entry<String, ComponentState> component : task.getValue())
				{
					final String componentName = component.getKey();

					if (componentName.equals("segment-labeling"))
					{

						if (component.getValue().isComplete())
						{
							output.println("<tr class='success'>");
						}
						else
						{
							output.println("<tr>");
						}
						output.println("<td>" + taskName + "</td>");

						int todoSegments = 0;
						int doneSegments = 0;
						
						/*TODO - look into code*/
						String temp = task.getValue().getProgress();
						temp = temp.replaceAll(" segments", "");
						
						String[]t = temp.split("/");
						doneSegments = Integer.parseInt(t[0]);
						todoSegments = Integer.parseInt(t[1]);
						
						output.print("<td class='right'>" + doneSegments + "/" + todoSegments + "</td>");
						
						output.print("<td class='right'>");
						double segRatio = 10000.0 * doneSegments / todoSegments;
						output.print("(" + String.valueOf(Math.round(segRatio) / 100.00) + "%)");
						output.print("</td>");
						
						output.print("<td class='center'>");
						
						int workTimeInSeconds = task.getValue().getAnnotatorWorkTimeInSeconds();
						int timeInMinutes = workTimeInSeconds / 60;
						int timeSec = workTimeInSeconds % 60;
						
						if (workTimeInSeconds != 0)
						{
							int timeInHours = timeInMinutes / 60;
							int timeMin = timeInMinutes % 60;
							
							output.print(String.valueOf(timeInHours) + ":");
							
							if (String.valueOf(timeMin).length() == 1)
							{
								output.print("0");
							}
							
							output.print(String.valueOf(timeMin) + ":");
							
							if (String.valueOf(timeSec).length() == 1)
							{
								output.print("0");
							}
							
							output.print(timeSec);
						}
						else
						{
							output.print("--");
						}

					output.print("</td>");
					output.println("</tr>");
					}

				}

			}

			output.println("</tbody>");
			output.println("</table>");
			output.println("<br/>");
		}
	}
}
