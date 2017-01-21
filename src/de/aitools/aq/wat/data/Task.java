package de.aitools.aq.wat.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.aitools.aq.wat.pages.AnnotatorPage;
import de.aitools.aq.wat.pages.AnnotatorTaskPage;

public class Task implements Comparable<Task>, Iterable<Component> {

  private final String name;
  
  private final List<Component> components;
  
  private final File taskStatesDirectory;
  
  private final AnnotatorPage page;

  public Task(final String name, final List<Component> components,
      final String projectName, final File tasksStatesDirectory)
  throws IOException {
    if (name == null) {
      throw new NullPointerException("Task name must not be null");
    }
    this.name = name;
    
    // load components
    this.components = Collections.unmodifiableList(components);
    
    // collect includes
    final List<String> cssIncludes = new ArrayList<>();
    final List<String> jsIncludes = new ArrayList<>();
    for (final Component component : this.components) {
      cssIncludes.addAll(component.getCssIncludes());
      jsIncludes.addAll(component.getJsIncludes());
    }
    this.page = new AnnotatorTaskPage(projectName, cssIncludes, jsIncludes);
    
    // task states directory
    this.taskStatesDirectory = new File(tasksStatesDirectory, this.name);
    if (!this.taskStatesDirectory.isDirectory()) {
      if (!this.taskStatesDirectory.mkdirs()) {
        throw new IOException("Could not create task states directory: "
      + this.taskStatesDirectory);
      }
    }
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.getName();
  }
  
  @Override
  public Iterator<Component> iterator() {
    return this.components.iterator();
  }

  @Override
  public int compareTo(final Task o) {
    return this.name.compareTo(o.name);
  }
  
  public AnnotatorPage getPage() {
    return this.page;
  }

  public TaskState loadState(final String annotatorName)
  throws NullPointerException, IOException {
    return new TaskState(this, this.taskStatesDirectory, annotatorName);
  }

}
