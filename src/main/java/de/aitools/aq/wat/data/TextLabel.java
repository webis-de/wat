package de.aitools.aq.wat.data;

public class TextLabel
{

	private String label;
	private String text;
	
	public TextLabel(String label, String text)
	{
		super();
		
		this.label = label;
		this.text = text;
		
	}
	
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
