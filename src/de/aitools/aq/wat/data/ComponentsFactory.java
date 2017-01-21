package de.aitools.aq.wat.data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.aitools.aq.wat.io.RecordsFile;

public class ComponentsFactory {
  
  private final List<Constructor<? extends Component>> constructors;
  
  private final List<String> componentNames;
  
  private final File projectDirectory;
  
  private final File tasksDirectory;
  
  private final boolean anonymizeAnnotatorsForCuration;
  
  public static ComponentsFactory load(final String[] componentEntries,
      final File projectDirectory, final File tasksDirectory,
      final boolean anonymizeAnnotatorsForCuration)
  throws IllegalArgumentException {
    final List<String> componentNames = new ArrayList<>();
    final List<String> classStrings = new ArrayList<>();

    for (final String componentEntry : componentEntries) {
      final String[] keyValue = componentEntry.split("=", 2);
      if (keyValue.length != 2) {
        throw new IllegalArgumentException(
            "No '<component-name>=<component-class>' pair: " + componentEntry);
      }
      componentNames.add(keyValue[0]);
      classStrings.add(keyValue[1]);
    }
    
    return new ComponentsFactory(componentNames, classStrings,
        projectDirectory, tasksDirectory, anonymizeAnnotatorsForCuration);
  }
  
  public ComponentsFactory(
      final List<String> componentNames, final List<String> classStrings,
      final File projectDirectory, final File tasksDirectory,
      final boolean anonymizeAnnotatorsForCuration)
  throws IllegalArgumentException {
    if (componentNames == null) {
      throw new NullPointerException("The component names must not be null");
    }
    if (classStrings == null) {
      throw new NullPointerException("The class strings must not be null");
    }
    if (projectDirectory == null) {
      throw new NullPointerException("The project directory must not be null");
    }
    if (tasksDirectory== null) {
      throw new NullPointerException("The tasks directory must not be null");
    }
    if (componentNames.size() != classStrings.size()) {
      throw new IllegalArgumentException(
          "Unequal number of component names and class strings: "
          + componentNames.size() + " != " + classStrings.size());
    }

    this.componentNames = componentNames;
    this.projectDirectory = projectDirectory;
    this.tasksDirectory = tasksDirectory;
    this.constructors = new ArrayList<>(this.componentNames.size());
    for (final String classString : classStrings) {
      this.constructors.add(this.getConstructor(classString));
    }
    this.anonymizeAnnotatorsForCuration = anonymizeAnnotatorsForCuration;
  }
  
  private Constructor<? extends Component> getConstructor(
      final String classString)
  throws IllegalArgumentException {
    try {
      final Class<?> componentClassGeneric = Class.forName(classString);
      if (Component.class.isAssignableFrom(componentClassGeneric)) {
        @SuppressWarnings("unchecked")
        final Class<? extends Component> componentClass =
            (Class<? extends Component>) componentClassGeneric;
        return componentClass.getConstructor(
            String.class, File.class, File.class, Map.class);
      } else {
        throw new IllegalArgumentException("Class " + classString
            + " does not implement " + Component.class.getName());
      }
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Class " + classString
          + " does not implement the required constructor for components");
    }
  }
  
  public List<Component> loadComponents(final String taskName)
  throws IOException {
    final File taskDirectory = new File(this.tasksDirectory, taskName);
    final Map<String, RecordsFile> recordFilesByAnnotator =
        this.loadRecordFilesByAnnotator(taskDirectory);

    final int size = this.componentNames.size();
    final List<Component> components = new ArrayList<>(size);

    for (int c = 0; c < size; ++c) {
      final String componentName = this.componentNames.get(c);
      final Constructor<? extends Component> constructor =
          this.constructors.get(c);

      try {
        final Component component = constructor.newInstance(componentName,
            this.projectDirectory, taskDirectory, recordFilesByAnnotator);
        components.add(component);
      } catch (final InvocationTargetException | InstantiationException
          | IllegalAccessException e) {
        throw new IllegalArgumentException(
            "Could not construct component from " + constructor, e);
      }
    }
    
    return components;
  }
  
  private Map<String, RecordsFile> loadRecordFilesByAnnotator(
      final File taskDirectory)
  throws IOException {
    final Map<String, RecordsFile> recordFilesByAnnotator = new HashMap<>();
    if (taskDirectory.isDirectory()) {
      for (final RecordsFile recordsFile 
          : RecordsFile.readAllInDirectory(taskDirectory)) {
        recordFilesByAnnotator.put(recordsFile.getAnnotator(), recordsFile);
      }
    }
    
    if (this.anonymizeAnnotatorsForCuration) {
      final List<String> annotators =
          new ArrayList<>(recordFilesByAnnotator.keySet());
      // implementing a shuffling that this consistent between server reloads
      Collections.sort(annotators);
      Collections.shuffle(
          annotators, new Random(taskDirectory.getName().hashCode()));
      
      int a = 1;
      for (final String annotator : annotators) {
        final String anonymized = "annotator" + a;
        final RecordsFile recordsFile = recordFilesByAnnotator.get(annotator);
        recordFilesByAnnotator.remove(annotator);
        recordFilesByAnnotator.put(anonymized, recordsFile);
        ++a;
      }
    }
    return recordFilesByAnnotator;
  }

}
