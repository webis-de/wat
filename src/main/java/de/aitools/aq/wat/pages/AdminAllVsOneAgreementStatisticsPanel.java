package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.distance.NominalDistanceFunction;

import de.aitools.aq.wat.WatProject;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.ComponentState;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;

public class AdminAllVsOneAgreementStatisticsPanel extends Panel {

	private static final String NAME = "All vs One Agreement Statistics";

	private final WatProject project;

	public AdminAllVsOneAgreementStatisticsPanel(final WatProject project) {
		super(NAME);
		if (project == null) {
			throw new NullPointerException();
		}
		this.project = project;
	}

	@Override
	protected void printPanelContent(PrintWriter output) {
		int numAnnotators = this.project.getAnnotatorNames().size();

		output.println("<table class='table'>");
		output.println("<thead>");
		output.print("<tr>");
		output.print("<th scope='col'>excluded annotator</th>");
		output.print("<th scope='col'>micro average</th>");
		output.print("<th scope='col'>macro average</th>");
		output.println("</tr>");
		output.print("</thead>");
		for (final Annotator exemptedAnnotator : this.project) {
			output.print("<tr>");
			// put names
			output.print("<th scope='row'>");
			output.print(exemptedAnnotator.getLoginName() + " (" + exemptedAnnotator.getName() + ")");
			output.print("</th>");
			// html code wird als Wert in der zurückgegebenen Map erzeugt -> alpha-Werte
			// extrahieren
			Map<Key, Labels> items = collectItemsAllVsOne(numAnnotators - 1, exemptedAnnotator);
			output.print(computeAndPrintAverages(computeTaskAgreement(items, numAnnotators - 1)));

			output.print("</tr>");
		}
		output.println("</table>");
		output.print(
				"Tasks completed by all but one annotator, are included in the calculations if that annotator is excluded.");
	}

	private class Labels {
		public String[] labels;
		public int numAnnotatedLabels;

		public Labels(int maxLabels) {
			this.labels = new String[maxLabels];
			this.numAnnotatedLabels = 0;
		}
	}

	private class Key implements Comparable<Key> {
		public final String task;
		public final String segment;

		public Key(String task, String segment) {
			this.task = task;
			this.segment = segment;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((segment == null) ? 0 : segment.hashCode());
			result = prime * result + ((task == null) ? 0 : task.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (segment == null) {
				if (other.segment != null)
					return false;
			} else if (!segment.equals(other.segment))
				return false;
			if (task == null) {
				if (other.task != null)
					return false;
			} else if (!task.equals(other.task))
				return false;
			return true;
		}

		private AdminAllVsOneAgreementStatisticsPanel getOuterType() {
			return AdminAllVsOneAgreementStatisticsPanel.this;
		}

		@Override
		public int compareTo(Key o) {
			int taskComp = this.task.compareTo(o.task);
			if (taskComp == 0) {
				return this.segment.compareTo(o.segment);
			}
			return taskComp;
		}
	}

	KrippendorffAlphaAgreement alpha = null;

	private Map<Key, Labels> collectItemsAllVsOne(int numAnnotators, Annotator exemptedAnnotator) {
		Map<Key, Labels> items = new HashMap<>();
		int annotatorIndex = 0;
		for (final Annotator annotator : this.project) {
			// Tasks that have not been processed by exactly one annotator appear in the micro-alpha for this exemptedAnnotator
			
			if (annotator == exemptedAnnotator) {
				continue;
			}
			for (final Entry<Task, TaskState> task : annotator) {
				for (final Entry<String, ComponentState> component : task.getValue()) {
					if (!component.getValue().isComplete()) {
						continue;
					}
					for (Entry<String, String> segment : component.getValue()) {
						Key key = new Key(task.getKey().getName(), segment.getKey());
						if (!items.containsKey(key)) {
							items.put(key, new Labels(numAnnotators));
						}
						Labels labels = items.get(key);
						labels.labels[annotatorIndex] = segment.getValue();
						labels.numAnnotatedLabels++;
						items.put(key, labels);
					}
				}
			}
			annotatorIndex++;
		}
		return items;
	}

	private TreeMap<String, Double> computeTaskAgreement(Map<Key, Labels> items, int numAnnotators) {
		CodingAnnotationStudy study = new CodingAnnotationStudy(numAnnotators);
		String currentTask = null;
		CodingAnnotationStudy taskStudy = null;
		List<Key> keyList = new ArrayList<>(items.keySet());

		Collections.sort(keyList);

		TreeMap<String, Double> outputMap = new TreeMap<String, Double>();

		for (Key key : keyList) {
			if (key.task != currentTask) {
				if (currentTask != null) {
					KrippendorffAlphaAgreement alphaCurrentTask = new KrippendorffAlphaAgreement(taskStudy,
							new NominalDistanceFunction());

					if (taskStudy.getItemCount() != 0) {
						outputMap.put(currentTask,
								new Double(Math.round(alphaCurrentTask.calculateAgreement() * 1000)));
					}
				}

				taskStudy = new CodingAnnotationStudy(numAnnotators);
				currentTask = key.task;
			}
			Labels labels = items.get(key);
			if (labels.numAnnotatedLabels != numAnnotators) {
				continue;
			}
			taskStudy.addItemAsArray(labels.labels);
			study.addItemAsArray(labels.labels);
		}

		// agreement of last task
		if (currentTask != null) {
			KrippendorffAlphaAgreement alpha = new KrippendorffAlphaAgreement(taskStudy, new NominalDistanceFunction());
			if (taskStudy.getItemCount() != 0) {
				outputMap.put(currentTask, new Double(Math.round(alpha.calculateAgreement() * 1000)));
			}
		}

		alpha = new KrippendorffAlphaAgreement(study, new NominalDistanceFunction());

		return outputMap;
	}

	private String computeAndPrintAverages(Map<String, Double> taskAgreements) {
		List<String> taskList = new ArrayList<String>(taskAgreements.keySet());
		int taskCount = 0;
		double agreementSum = 0;
		String output = "";
		for (final String currentTask : taskList) {
			taskCount++;
			agreementSum += taskAgreements.get(currentTask);
		}
		if (taskCount != 0) {
			double agreementAverage = Math.round(agreementSum / taskCount);
			output = "<td class='center'>" + agreementAverage / 1000 + "</td>";
		}
		double macroAlpha = Math.round(alpha.calculateAgreement() * 1000);
		output += "<td class='center'>" + macroAlpha / 1000 + "</td>";

		return output;
	}

}
