package de.aitools.aq.wat.pages;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.distance.NominalDistanceFunction;

import de.aitools.aq.wat.WatProject;
import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.ComponentState;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;

public class AdminAgreementStatisticsPanel extends Panel {
	private static final String NAME = "Inter Annotator Agreement Statistics";
	private final WatProject project;

	public AdminAgreementStatisticsPanel(WatProject project) {
		super(NAME);

		if (project == null) {
			throw new NullPointerException();
		}

		this.project = project;
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

		private AdminAgreementStatisticsPanel getOuterType() {
			return AdminAgreementStatisticsPanel.this;
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
	
	private DescriptiveStatistics stats = new DescriptiveStatistics();
	KrippendorffAlphaAgreement alpha = null;

	private Map<Key, Labels> collectItems(int numAnnotators)
	{
		Map<Key, Labels> items = new HashMap<>();
		int annotatorIndex = 0;
		for (final Annotator annotator : this.project)
		{
			for (final Entry<Task, TaskState> task : annotator)
			{
				for (final Entry<String, ComponentState> component : task.getValue())
				{
					if (!component.getValue().isComplete())
					{
						continue;
					}
					for (Entry<String, String> segment : component.getValue())
					{
						Key key = new Key(task.getKey().getName(), segment.getKey());
						if (!items.containsKey(key))
						{
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

	private TreeMap<String, String> collectPrintIncompleteItems(int numAnnotators)
	{
		TreeMap<String, String> items = new TreeMap<>();

		HashSet <String> unFinSegments = new HashSet<String>();
		HashSet <String> unFinAnnotators = new HashSet<String>();
		TreeMap<String, Integer> annSegmentsMap = new TreeMap<String, Integer>();
		TreeMap<String, Integer> allSegmentsMap = new TreeMap<String, Integer>();
		TreeMap<String, Integer> unFinAnnotatorsMap = new TreeMap<String, Integer>();

		for (final Annotator annotator : this.project)
		{
			for (final Entry<Task, TaskState> task : annotator)
			{
				for (final Entry<String, ComponentState> component : task.getValue())
				{
					if (!component.getValue().isComplete())
					{
						unFinSegments.add(task.getKey().toString());
						unFinAnnotators.add(annotator.getName());
					}
					break;
				}
			}
		}

		for (final Annotator annotator : this.project)
		{
			for (final Entry<Task, TaskState> task : annotator)
			{
				for (final Entry<String, ComponentState> component : task.getValue())
				{
					if (unFinSegments.contains(task.getKey().toString()))
					{
						String currentTask = task.getKey().toString();
						String[] s = task.getValue().getProgress().toString().replaceAll("segments", "").replaceAll(" ", "").split("/");

						if ( ! (annSegmentsMap.containsKey(currentTask)) )
						{
							annSegmentsMap.put(currentTask, Integer.parseInt(s[0]));
							allSegmentsMap.put(currentTask, Integer.parseInt(s[1]));
						}
						else
						{
							try {
								Integer annSeg = 0;
								Integer allSeg = 0;

								annSeg = annSegmentsMap.get(currentTask);
								allSeg = allSegmentsMap.get(currentTask);

								Integer annSegNew = Integer.parseInt(s[0]);
								Integer allSegNew = Integer.parseInt(s[1]);

								annSeg = annSeg + annSegNew;
								allSeg = allSeg + allSegNew;

								annSegmentsMap.put(currentTask, annSeg);
								allSegmentsMap.put(currentTask, allSeg);

							} catch (Exception e) {}
						}
						
						if (!(s[0].equals(s[1])))
						{
							if ( ! (unFinAnnotatorsMap.containsKey(currentTask)) )
							{
								unFinAnnotatorsMap.put(currentTask, 1);
							}
							else
							{
								Integer annotators = 0;
								annotators = unFinAnnotatorsMap.get(currentTask);
								unFinAnnotatorsMap.put(currentTask, annotators + 1);
							}
						}
						
					}
					break;
				}
			}
		}

		for (String currentTask : annSegmentsMap.keySet())
		{
			Double segmentsCurrentTask = Double.valueOf(allSegmentsMap.get(currentTask));
			double segmentRatio = Math.round((annSegmentsMap.get(currentTask) * 100) / segmentsCurrentTask);
			Double unFinAnno = Double.valueOf(numAnnotators - unFinAnnotatorsMap.get(currentTask));
			double finAnnotatorsRatio = Math.round(( unFinAnno * 100) / numAnnotators);
			
			StringBuilder output = new StringBuilder();

			output.append("<tr>");
			output.append("<td class='right'>" + currentTask + "</td>\n"); // key
			output.append("<td class='right'>" + annSegmentsMap.get(currentTask) + "/" + allSegmentsMap.get(currentTask)
								+ "</td><td class='right'>(" + segmentRatio + "%) " + "</td>\n");
			
			output.append("<td class='right'>" + (numAnnotators - unFinAnnotatorsMap.get(currentTask)) + "/" + numAnnotators
								+ "</td><td class='right'>(" + finAnnotatorsRatio + "%)" + "</td>\n"); 

			output.append("<td class='right'>n.n.</td>");
			output.append("</tr>");
			items.put(currentTask, output.toString());
		}

		return items;
	}

	private TreeMap<String, String> computeAgreement(Map<Key, Labels> items, int numAnnotators)
	{
		CodingAnnotationStudy study = new CodingAnnotationStudy(numAnnotators);
		String currentTask = null;
		CodingAnnotationStudy taskStudy = null;
		List<Key> keyList = new ArrayList<>(items.keySet());
		
		Collections.sort(keyList);

		TreeMap<String, String> outputMap = new TreeMap<String, String>();
		
		for (Key key : keyList)
		{
			if (key.task != currentTask)
			{
				if (currentTask != null)
				{
					KrippendorffAlphaAgreement alphaCurrentTask = new KrippendorffAlphaAgreement(taskStudy, new NominalDistanceFunction());
					
					if (taskStudy.getItemCount() != 0)
					{
						outputMap.put(currentTask, printAgreement(currentTask, taskStudy.getUnitCount(), taskStudy.getRaterCount(), alphaCurrentTask));
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
			if (taskStudy.getItemCount() != 0){
				outputMap.put(currentTask, printAgreement(currentTask, taskStudy.getUnitCount(), taskStudy.getRaterCount(), alpha));
			}
		}

		alpha = new KrippendorffAlphaAgreement(study, new NominalDistanceFunction());

		return outputMap;
	}

	private String printAgreement(String currentTask, int countSegments, int unitCounts, KrippendorffAlphaAgreement alpha)
	{
		StringBuilder output = new StringBuilder();
		
		output.append("<tr class='success'>");
		
		output.append("<td class='right'>" + currentTask + "</td>"); // key
		output.append("<td class='right'>" + countSegments + "/" + countSegments + "</td><td class='right'>(100.0%) " + "</td>");  // segFin + "/" + segAll .. ratioSegS
		output.append("<td class='right'>" + unitCounts + "/" + unitCounts + "</td><td class='right'>(100.0%)" + "</td>"); // annoFin + "/" + annoAll .. ratioFinS
		
		double alphaValue = alpha.calculateAgreement();
		double avgAlpha = Math.round(alphaValue * 1000);
		output.append("<td class='right'><b>" + avgAlpha / 1000 + "</b></td>");
		output.append("</tr>");
		
		stats.addValue(alphaValue);

		return output.toString();
	}

	@Override
	protected void printPanelContent(final PrintWriter output) {
		int numAnnotators = this.project.getAnnotatorNames().size();
		Map<Key, Labels> items = collectItems(numAnnotators);
		
		output.println("<table class='table'>");
		output.println("<thead>");
		output.print("<tr>");
		output.print("<th scope='col' class='right'>Task</th>");
		output.print("<th scope='col' colspan='2' class='center'>Segments</th>");
		output.print("<th scope='col' colspan='2' class='center'>Annotators</th>");
		output.print("<th scope='col' class='right'>Kripp. Alpha</th>");
		output.println("</tr>");
		output.println("</thead>");
		output.println("<tbody>");

		TreeMap<String, String> outputMap = computeAgreement(items, numAnnotators);
		TreeMap<String, String> outputMapIncompleteItems = collectPrintIncompleteItems(numAnnotators);

		outputMap.putAll(outputMapIncompleteItems);
		
		for (String task : outputMap.keySet())
		{
			output.append(outputMap.get(task));
		}

		output.println("</tbody>");
		output.println("</table>");
		output.println("<br/>");

		output.println("<table class='table'>");
		output.println("<thead>");
		output.println("<tr><th scope='col' class='right'>measure</th><th scope='col'>" + "value" + "</td></th>");
		output.println("</thead>");

		double maxVal = Math.round(stats.getMax() * 1000);
		output.println("<tr><td class='right'>max</td><td>" + maxVal / 1000 + "</td></tr>");

		double minVal = Math.round(stats.getMin() * 1000);
		output.println("<tr><td class='right'>min</td><td>" + minVal / 1000 + "</td></tr>");

		double avg = Math.round(stats.getMean() * 1000);
		output.println("<tr><td class='right'>average</td><td>" + avg / 1000 + "</td></tr>");
		
		double std = Math.round(stats.getStandardDeviation() * 1000);
		output.println("<tr><td class='right'>standard deviavtion</td><td>" + std / 1000 + "</td></tr>");
		
		double loQuartile = Math.round(stats.getPercentile(25) * 1000);
		output.println("<tr><td class='right'>lower quartile</td><td>" + loQuartile / 1000 + "</td></tr>");
		
		double median = Math.round(stats.getPercentile(50) * 1000);
		output.println("<tr><td class='right'>median </td><td>" + median / 1000 + "</td></tr>");
		
		double upQuartile = Math.round(stats.getPercentile(75) * 1000);
		output.println("<tr><td class='right'>upper quartile</td><td>" + upQuartile / 1000 + "</td></tr>");

		double alphaDouble = Math.round( alpha.calculateAgreement() * 1000);
		output.println("<tr><td class='right'>Krippendorff's Alpha over all finished tasks (macro avg.)</td><td>" + alphaDouble / 1000 + "</td></tr>");


		output.println("</tbody>");
		output.println("</table>");
		output.println("<br/>");
	}
}
