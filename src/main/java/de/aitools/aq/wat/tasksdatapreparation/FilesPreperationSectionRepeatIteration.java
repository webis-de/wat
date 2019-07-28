package de.aitools.aq.wat.tasksdatapreparation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FilesPreperationSectionRepeatIteration {
	public static final String RAW_DATA = "tasks-data"; // text files
	public static final String TASKS = "tasks"; // directories with task files

	public static void main(String[] args) throws Exception
	{
		if (Files.notExists(Paths.get(TASKS)))
		{
			Files.createDirectory(Paths.get(TASKS));
		}

		Stream<Path> taskStatesFiles = Files
			.walk(Paths.get(RAW_DATA))
			.filter(s -> s.toString()
			.endsWith("txt"))
			.sorted();

		for (Iterator<Path> iterator = taskStatesFiles.iterator(); iterator.hasNext();)
		{
			Path path = iterator.next();

			String[] p = path.toString().split(Pattern.quote(System.getProperty("file.separator")));
			String taskName = p[p.length - 1].replaceAll(".txt", "");
			Path taskDirName = Paths.get(TASKS + "/" + taskName);

			if (Files.notExists(taskDirName))
			{
				Files.createDirectory(taskDirName);
			}

			Path outConf = Paths.get(taskDirName + "/" + "segment-labeling.conf");
			Path outTxt = Paths.get(taskDirName + "/" + "segment-labeling.txt");
			Files.copy(path, outTxt);
			Files.write(outConf, taskName.getBytes("UTF-8"));
		}
	}
}