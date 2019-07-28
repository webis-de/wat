package de.aitools.aq.wat.data;

public class Annotation
{
	private final String task;
	private final Integer segment;
	private final String annotatorName;

	public Annotation(String task, int segment, String annotatorName) 
	{
		this.task = task;
		this.segment = segment;
		this.annotatorName = annotatorName;
	}

	public String getTask()
	{
		return task;
	}

	public int getSegment()
	{
		return segment;
	}

	public String getAnnotatorName()
	{
		return annotatorName;
	}
}
