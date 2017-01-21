package de.aitools.aq.wat.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public abstract class ComponentState
implements Iterable<Entry<String, String>> {
  
  private final Map<String, String> valueMap;
  
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
  
  public void writeKeyValues(final Writer writer, final String componentName)
  throws IOException {
    final Set<String> keys = new TreeSet<>(this.valueMap.keySet());
    for (final String key : keys) {
      writer.append(componentName).append('.').append(key).append(" = ")
        .append(this.valueMap.get(key)).append('\n');
    }
  }

}
