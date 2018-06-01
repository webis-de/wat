package de.aitools.aq.wat.data;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.aitools.aq.wat.io.Client;
import de.aitools.aq.wat.io.RecordsFile;
import de.aitools.aq.wat.io.RecordsFile.Record;

public class TaskState implements Iterable<Entry<String, ComponentState>> {

  //----------------------------------------------------------------------//
  //                            Member variables                          //
  //----------------------------------------------------------------------//
  
  private final Task task;
  
  private final Map<String, ComponentState> componentStates;

  private final RecordsFile recordFile;


  //----------------------------------------------------------------------//
  //                              Construction                            //
  //----------------------------------------------------------------------//

  public TaskState(final Task task,
      final File taskStateDirectory, final String annotatorName)
  throws NullPointerException, IOException {
    this.task = task;
    
    final Map<String, ComponentState> componentStates = new HashMap<>();
    for (final Component component : task) {
      componentStates.put(component.getName(), component.createNewState());
    }
    this.componentStates = Collections.unmodifiableMap(componentStates);
    
    this.recordFile =
        new RecordsFile(taskStateDirectory, annotatorName);
    // load component states
    for (final Record record : this.recordFile) {
      final ComponentState componentState =
          this.componentStates.get(record.getComponentName());
      if (componentState == null) {
        throw new IllegalArgumentException(
            "No such component: " + record.getComponentName());
      }
      componentState.updateValue(record.getKey(), record.getValue());
    }
  }

  //----------------------------------------------------------------------//
  //                               Get state                              //
  //----------------------------------------------------------------------//
  
  public Task getTask() {
    return this.task;
  }

  public boolean isComplete() {
    for (final Component component : this.task) {
      if (component.isRequired()) {
        if (!this.componentStates.get(component.getName()).isComplete()) {
          return false;
        }
      }
    }
    
    return true;
  }

  public String getProgress() {
    final StringBuilder progressBuilder = new StringBuilder();
    for (final Component component : this.task) {
      if (component.isRequired()) {
        final ComponentState componentState =
            this.componentStates.get(component.getName());
        final String subProgress = componentState.getProgress();
        if (!subProgress.isEmpty()) {
          if (progressBuilder.length() > 0) {
            progressBuilder.append(", ");
          }
          progressBuilder.append(subProgress);
        }
      }
    }
    return progressBuilder.toString();
  }

  public Date getLastUpdateInLocalTime() {
    return this.recordFile.getLastUpdateInLocalTime();
  }

  public int getAnnotatorWorkTimeInSeconds() {
    return this.recordFile.getAnnotatorWorkTimeInSeconds();
  }
  
  @Override
  public Iterator<Entry<String, ComponentState>> iterator() {
    return this.componentStates.entrySet().iterator();
  }

  public void writeKeyValues(final Writer writer)
  throws IOException {
    final Set<String> componentNames =
        new TreeSet<>(this.componentStates.keySet());
    for (final String componentName : componentNames) {
      this.componentStates.get(componentName).writeKeyValues(
          writer, componentName);
    }
  }

  //----------------------------------------------------------------------//
  //                             Modify state                             //
  //----------------------------------------------------------------------//

  public void workerOpenedTask(final Client client, final int timeZoneOffset)
  throws IOException {
    this.recordFile.openTask(client, timeZoneOffset);
  }

  public void setValue(final String componentName,
      final String key, final String value,
      final Client client, final int timeZoneOffset)
  throws NullPointerException, IllegalArgumentException, IOException {
    this.recordFile.annotate(componentName, key, value, client, timeZoneOffset);
    this.componentStates.get(componentName).updateValue(key, value);
  }


}
