package de.aitools.aq.wat.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Annotator implements Iterable<Entry<Task, TaskState>> {

  private final String name;

  private final String loginName;

  private final String password;

  private final Map<String, Task> taskMap;

  private final Map<Task, TaskState> stateMap;

  public Annotator(final String name, final String loginName,
      final String password, final Map<Task, TaskState> stateMap)
  throws IOException {
    if (name == null) {
      throw new NullPointerException("Worker name must not be null");
    }
    this.name = name;

    if (loginName == null) {
      throw new NullPointerException("Worker login name must not be null");
    }
    this.loginName = loginName;

    if (password == null) {
      throw new NullPointerException("Worker password must not be null");
    }
    this.password = password;

    if (stateMap == null) {
      throw new NullPointerException("State map must not be null");
    }
    this.stateMap = Collections.unmodifiableMap(stateMap);

    this.taskMap = new TreeMap<String, Task>();
    for (final Task task : this.stateMap.keySet()) {
      this.taskMap.put(task.getName(), task);
    }
  }

  public String getName() {
    return this.name;
  }

  public String getLoginName() {
    return this.loginName;
  }

  public String getPassword() {
    return this.password;
  }

  public boolean checkPassword(final String password) {
    return this.password.equals(password);
  }

  public Task getTask(final String taskName) {
    if (taskName == null) { return null; }
    return this.taskMap.get(taskName);
  }

  public TaskState getState(final Task task)
  throws IOException {
    if (task == null) {
      throw new NullPointerException("Task must not be null");
    }
    return this.stateMap.get(task);
  }

  @Override
  public Iterator<Entry<Task, TaskState>> iterator() {
    return this.stateMap.entrySet().iterator();
  }

}
