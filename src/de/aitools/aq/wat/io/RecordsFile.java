package de.aitools.aq.wat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class RecordsFile implements Iterable<RecordsFile.Record> {
  
  private static final String FILE_SUFFIX = "-records.txt";

  public static final String FIELD_SEPARATOR = "\t";

  private static final DateFormat DATE_FORMAT =
      RecordsFile.compileDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");

  private static DateFormat compileDateFormat(final String format) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat;
  }
  
  private final File file;
  
  private final List<Record> nonInternalRecords;
  
  private final List<Record> nonInternalRecordsReadOnly;
  
  public RecordsFile(final File directory, final String annotator)
  throws IOException {
    this(new File(directory, annotator + FILE_SUFFIX));
  }
  
  private RecordsFile(final File file)
  throws IOException {
    this.file = file;
    this.nonInternalRecords = new ArrayList<>();
    this.nonInternalRecordsReadOnly =
        Collections.unmodifiableList(this.nonInternalRecords);
    
    if (this.file.exists()) {
      try (final BufferedReader reader =
          new BufferedReader(new FileReader(this.file))) {
        String line = null;
        while ((line = reader.readLine()) != null) {
          try {
            final Record record = Record.read(line);
            if (!record.isInternal()) {
              this.nonInternalRecords.add(record);
            }
          } catch (final ParseException e) {
            throw new IOException(e);
          }
        }
      }
    }
  }
  
  public static List<RecordsFile> readAllInDirectory(final File directory)
  throws IOException {
    final List<RecordsFile> recordsFiles = new ArrayList<>();
    for (final File file : directory.listFiles()) {
      if (file.getName().endsWith(FILE_SUFFIX)) {
        recordsFiles.add(new RecordsFile(file));
      }
    }
    return recordsFiles;
  }
  
  public String getAnnotator() {
    final String filename = file.getName();
    return filename.substring(0, filename.length() - FILE_SUFFIX.length());
  }
  
  public Date getLastUpdateInLocalTime() {
    final long millisInMinute = 60 * 1000;
    final Record lastRecord = this.getLastRecord();
    if (lastRecord == null) {
      return null;
    } else {
      final Date date = new Date();
      date.setTime(lastRecord.date.getTime()
          - lastRecord.timeZoneOffset * millisInMinute);
      return date;
    }
  }
  
  private Record getLastRecord() {
    if (this.nonInternalRecords.isEmpty()) {
      return null;
    } else {
      synchronized (this) {
        return this.nonInternalRecords.get(this.nonInternalRecords.size() - 1);
      }
    }
  }
  
  @Override
  public Iterator<Record> iterator() {
    return this.nonInternalRecordsReadOnly.iterator();
  }
  
  public void annotate(final String componentName,
      final String key, final String value,
      final String client, final int timeZoneOffset)
  throws IOException {
    if (key.equals(Record.COMPONENT_NAME_INTERNAL)) {
      throw new IllegalArgumentException(
          "The component name '" + key + "' is reserved");
    }
    synchronized (this) {
      final Record record = new Record(
          new Date(), componentName, key, value, client, timeZoneOffset);
      record.write(this.file);
      this.nonInternalRecords.add(record);
    }
  }
  
  public void openTask(final String client, final int timeZoneOffset)
  throws IOException {
    synchronized (this) {
      final Record record = new Record(client, timeZoneOffset);
      record.write(this.file);
    }
  }
  
  public static String getAnnotatorName(final File recordsFile) {
    final String fileName = recordsFile.getName();
    if (!fileName.endsWith(FILE_SUFFIX)) {
      throw new IllegalArgumentException("Not a records file: " + recordsFile);
    }
    return fileName.substring(0, fileName.length() - FILE_SUFFIX.length());
  }
  
  
  public static class Record {
    
    private static final String COMPONENT_NAME_INTERNAL = "INFO";
    
    private static final String INTERNAL_KEY_ACTION = "action";
    
    private static final String INTERNAL_ACTION_OPEN = "open";
    
    private final Date date;
    
    private final int timeZoneOffset;
    
    private final String client;
    
    private final String componentName;
    
    private final String key;
    
    private final String value;
    
    private Record(final String client, final int timeZoneOffset) {
      this(new Date(), COMPONENT_NAME_INTERNAL,
          INTERNAL_KEY_ACTION, INTERNAL_ACTION_OPEN, client, timeZoneOffset);
    }
    
    private Record(
        final Date date, final String componentName,
        final String key, final String value,
        final String client, final int timeZoneOffset) {
      if (date == null) {
        throw new NullPointerException("The date must not be null");
      }
      if (client == null) {
        throw new NullPointerException("The client must not be null");
      }
      if (componentName == null) {
        throw new NullPointerException("The component name must not be null");
      }
      if (key == null) {
        throw new NullPointerException("The key must not be null");
      }
      if (value == null) {
        throw new NullPointerException("The value must not be null");
      }
      this.date = date;
      this.timeZoneOffset = timeZoneOffset;
      this.client = client;
      this.componentName = componentName;
      this.key = key;
      this.value = value.replaceAll("\n", "\\\\n");
    }
    
    private static Record read(final String line) throws ParseException {
      final String fields[] = line.split(FIELD_SEPARATOR, 6);
      final Date date = DATE_FORMAT.parse(fields[0]);
      final int timezoneOffset = Integer.parseInt(fields[1]);
      final String client = fields[2];
      final String componentName = fields[3];
      final String key = fields[4];
      final String value = fields[5];
      return new Record(date, componentName, key, value, client, timezoneOffset);
    }

    public String getComponentName() {
      return this.componentName;
    }
    
    public String getKey() {
      return this.key;
    }
    
    public String getValue() {
      return this.value;
    }
    
    private boolean isInternal() {
      return this.componentName.equals(COMPONENT_NAME_INTERNAL);
    }
    
    private void write(final File file) throws IOException {
      try (final FileWriter writer = new FileWriter(file, true)) {
        this.write(writer);
      }
    }
    
    private void write(final Writer writer) throws IOException {
      writer.append(DATE_FORMAT.format(this.date)).append(FIELD_SEPARATOR)
        .append(String.valueOf(this.timeZoneOffset)).append(FIELD_SEPARATOR)
        .append(this.client).append(FIELD_SEPARATOR)
        .append(this.componentName).append(FIELD_SEPARATOR)
        .append(this.key).append(FIELD_SEPARATOR)
        .append(this.value).append("\n");
    }
    
  }

}
