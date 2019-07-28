package de.aitools.aq.wat.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFiles {
  
  public static final String SUFFIX = ".conf";
  
  private PropertiesFiles() { }
  
  public static Properties load(final File file)
  throws IOException {
    return PropertiesFiles.load(new Properties(), file);
  }
  
  public static Properties load(final Properties defaults, final File file)
  throws IOException {
    final Properties properties = new Properties(defaults);
    if (file.exists()) {
      try (final FileInputStream input = new FileInputStream(file)) {
        properties.load(input);
      }
    }
    return properties;
  }

}
