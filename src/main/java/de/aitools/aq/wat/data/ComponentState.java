package de.aitools.aq.wat.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import de.aitools.aq.wat.components.TextLabel;

public abstract class ComponentState implements Iterable<Entry<String, String>> {

	private final Map<String, String> valueMap;

	static final String[] HEADER = { "text", "label"};
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	public ComponentState() {
		this.valueMap = new LinkedHashMap<String, String>();
	}

	public String getProgress() {
		if (this.isComplete()) {
			return "Complete";
		} else {
			return "Incomplete";
		}
	}

	public abstract boolean isComplete();

	public void updateValue(final String key, final String value) {
		this.updateProgress(key, value);
		this.valueMap.remove(key);
		this.valueMap.put(key, value);
	}

	public String getValue(final String key) {
		return this.valueMap.get(key);
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		return this.valueMap.entrySet().iterator();
	}

	protected abstract void updateProgress(final String key, final String value);

	public void writeKeyValues(final Writer writer, final String componentName) throws IOException
	{
		if (componentName.equals("segment-labeling"))
		{
			final Set<String> keys = new TreeSet<>(this.valueMap.keySet());

			TreeMap<Integer, String> segmentedLabels = new TreeMap<Integer, String>();

			for (final String key : keys)
			{
				int k = Integer.parseInt(key);
				segmentedLabels.put(k, componentName + "." + key + " = " + this.valueMap.get(key));
			}

			for (Entry<Integer, String> entry : segmentedLabels.entrySet() )
			{
				writer.append(entry.getValue()).append('\n');
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void writeKeySegmentDetailedValuesAnn(
			final Writer writer,
			final String componentName,
			final Task task,
			final String tasksDirectoryName,
			final String annotatorName,
			final String annotatedSegmentsDirectoryName
		) throws IOException
	{
		if (componentName.equals("segment-labeling"))
		{
			final Set<String> keys = new TreeSet<>(this.valueMap.keySet());

			TreeMap<Integer, String> segmentedLabels = new TreeMap<Integer, String>();

			// sort keys
			for (final String key : keys)
			{
				int k = Integer.parseInt(key);
				segmentedLabels.put(k, this.valueMap.get(key));
			}

			TreeMap<Integer, String> textSegments = new TreeMap<Integer, String>();
			int i = 0;

			// segments
			@SuppressWarnings("resource")
			Stream<String> lines = Files.lines(Paths.get(tasksDirectoryName + "/" + task.getName() + "/segment-labeling.txt"));
			
			for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();)
			{
				String line = iterator.next().toString();
				
				if (!(line.equals("")))
				{
					textSegments.put(i, line);
					i++;
				}
			}
			
			ArrayList<TextLabel> mapTextLabel = new ArrayList<TextLabel>();
			
			int index = 0;
			
			int offsetStart = 0;
			int offsetEnd = 0;
			
			// write content of BRAT-File.
			for (Entry<Integer, String> entry : segmentedLabels.entrySet() )
			{
				offsetEnd = offsetStart + entry.getValue().length() - 1;
				writer.append("T" + index + "\t" + entry.getValue() + " " + offsetStart + " " + offsetEnd + "\t" + textSegments.get(entry.getKey()) ).append('\n');
				
				index++;
				
				TextLabel tl = new TextLabel(entry.getValue(), textSegments.get(entry.getKey()));
				
				mapTextLabel.add(tl);
				
				offsetStart = offsetStart + entry.getValue().length() + 1;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void writeKeySegmentDetailedValuesCsv(
			final String componentName,
			final Task task,
			final String tasksDirectoryName,
			final String annotatorName,
			final String annotatedSegmentsDirectoryName
		) throws IOException
	{
		if (componentName.equals("segment-labeling"))
		{
			final Set<String> keys = new TreeSet<>(this.valueMap.keySet());

			TreeMap<Integer, String> segmentedLabels = new TreeMap<Integer, String>();

			// sort keys
			for (final String key : keys)
			{
				int k = Integer.parseInt(key);
				segmentedLabels.put(k, this.valueMap.get(key));
			}

			TreeMap<Integer, String> textSegments = new TreeMap<Integer, String>();
			int i = 0;

			// segments
			@SuppressWarnings("resource")
			Stream<String> lines = Files.lines(Paths.get(tasksDirectoryName + "/" + task.getName() + "/segment-labeling.txt"));
			
			for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();)
			{
				String line = iterator.next().toString();
				
				if (!(line.equals("")))
				{
					textSegments.put(i, line);
					i++;
				}
			}

			ArrayList<TextLabel> mapTextLabel = new ArrayList<TextLabel>();
			
			// write content of BRAT-File.
			for (Entry<Integer, String> entry : segmentedLabels.entrySet() )
			{
				TextLabel tl = new TextLabel(entry.getValue(), textSegments.get(entry.getKey()));
				
				mapTextLabel.add(tl);
			}

			FileWriter fileWriter = null;
			CSVPrinter csvFilePrinter = null;
			//Create the CSVFormat object with "\n" as a record delimiter

			CSVFormat csvFileFormat = CSVFormat
					.DEFAULT
					.withRecordSeparator(NEW_LINE_SEPARATOR)
					.withDelimiter('\t')
					.withQuote('"')
					;

			try
			{
				//initialize FileWriter object
				fileWriter = new FileWriter(annotatedSegmentsDirectoryName + "/" + task.getName() + "/" + annotatorName + ".csv");

				//initialize CSVPrinter object
				csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
				
				//Create CSV file header
				csvFilePrinter.printRecord(HEADER);
				
				for (int k = 0; k < mapTextLabel.size(); k++)
				{
					List r = new ArrayList<>();
					r.add(mapTextLabel.get(k).getText());
					r.add(mapTextLabel.get(k).getLabel());
					csvFilePrinter.printRecord(r);
				}
			}
			catch (Exception e)
			{
				System.out.println("Error in CsvFileWriter !!!");
				e.printStackTrace();
		
			}
			finally
			{
		
				try
				{
					fileWriter.flush();
					fileWriter.close();
					csvFilePrinter.close();
				}
				catch (IOException e)
				{
					System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
					e.printStackTrace();
				}
			}
		}
	}
}
