package de.aitools.aq.wat.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.aitools.aq.wat.io.PropertiesFiles;
import de.aitools.aq.wat.io.RecordsFile;
import de.aitools.aq.wat.io.RecordsFile.Record;

public abstract class Component implements Iterable<Component.Unit> {
  
  private static final String CONFIG_FIELD_REQUIRED = "required";
  
  private static final String CONFIG_FIELD_USE_MAJORITY_ANNOTATION = "curation.use.majority";
  
  private static final String CONFIG_FIELD_TITLE = "title";
  
  private static final String CONFIG_FIELD_DESCRIPTION = "description";
  
  private static final String CONFIG_FIELD_CSS_INCLUDES = "include.css";
  
  private static final String CONFIG_FIELD_JS_INCLUDES = "include.js";
  
  private final String name;
  
  private final String jsValueFunctionName;
  
  private boolean required;
  
  private boolean useMajorityAnnotation;

  private String title;

  private String description;
  
  private final List<String> cssIncludes;
  
  private final List<String> jsIncludes;
  
  private List<String> jsInitFunctionNames;
  
  private final List<Unit> units;
  
  public Component(
      final String name,
      final File projectDirectory, final File taskDirectory, 
      final Map<String, RecordsFile> recordFilesByAnnotator,
      final String jsValueFunctionName)
  throws IOException {
    if (name == null) {
      throw new NullPointerException("The name must not be null");
    }
    if (jsValueFunctionName == null) {
      throw new NullPointerException(
          "The Javascript value function name must not be null");
    }
    this.name = name;
    this.jsValueFunctionName = jsValueFunctionName;
    this.required = true;
    this.useMajorityAnnotation = true;
    this.title = null;
    this.description = null;
    this.cssIncludes = new ArrayList<>();
    this.jsIncludes = new ArrayList<>();
    this.jsInitFunctionNames = new ArrayList<>();
    this.units = new ArrayList<>();

    final File projectConfigFile =
        new File(projectDirectory, this.getName() + PropertiesFiles.SUFFIX);
    final File taskConfigFile =
        new File(taskDirectory, this.getName() + PropertiesFiles.SUFFIX);
    final Properties config = PropertiesFiles.load(
        PropertiesFiles.load(projectConfigFile), taskConfigFile);
    
    this.loadForTask(config, projectDirectory, taskDirectory);
    this.applyConfig(config);
    this.addCurationInfo(recordFilesByAnnotator);
  }
  
  protected abstract void loadForTask(
      final Properties config,
      final File projectDirectory, final File taskDirectory)
  throws IOException;
  
  protected void applyConfig(final Properties config) {
    this.setRequired(Boolean.parseBoolean(
        config.getProperty(CONFIG_FIELD_REQUIRED, "true")));
    this.setUseMajorityAnnotation(Boolean.parseBoolean(
        config.getProperty(CONFIG_FIELD_USE_MAJORITY_ANNOTATION, "true")));
    this.setTitle(config.getProperty(CONFIG_FIELD_TITLE));
    this.setDescription(config.getProperty(CONFIG_FIELD_DESCRIPTION));
    this.addCssIncludes(
        config.getProperty(CONFIG_FIELD_CSS_INCLUDES, "").split("\\s+"));
    this.addJsIncludes(
        config.getProperty(CONFIG_FIELD_JS_INCLUDES, "").split("\\s+"));
  }
  
  protected void addCurationInfo(
      final Map<String, RecordsFile> recordFilesByAnnotator)
  throws IOException {
    for (final Entry<String, RecordsFile> entry
        : recordFilesByAnnotator.entrySet()) {
      final String annotator = entry.getKey();
      final RecordsFile recordsFile = entry.getValue();
      final Stream<Record> records = StreamSupport
          .stream(recordsFile.spliterator(), false)
          .filter(record -> record.getComponentName().equals(this.getName()));
      this.addCurationInfo(annotator, records);
    }
    
    for (final Unit unit : this) {
      unit.checkMajorityAnnotation();
    }
  }

  protected void addCurationInfo(
      final String annotator, final Stream<Record> records)
  throws IOException {
    final Map<String, String> annotations = new HashMap<>();
    records.forEachOrdered(
        record -> annotations.put(record.getKey(), record.getValue()));
    
    for (final Unit unit : this) {
      final String key = unit.getKey();
      final String label = annotations.get(key);
      if (label != null) {
        unit.addAnnotation(annotator, label);
      }
    }
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getJsValueFunctionName() {
    return this.jsValueFunctionName;
  }
  
  public boolean isRequired() {
    return this.required;
  }
  
  public boolean usesMajorityAnnotation() {
    return this.useMajorityAnnotation;
  }
  
  public String getTitle() {
    return this.title;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public List<String> getCssIncludes() {
    return this.cssIncludes;
  }
  
  public List<String> getJsIncludes() {
    return this.jsIncludes;
  }
  
  public List<String> getJsInitFunctionNames() {
    return this.jsInitFunctionNames;
  }
  
  public int getSize() {
    return this.units.size();
  }

  @Override
  public Iterator<Unit> iterator() {
    return this.units.iterator();
  }
  
  @Override
  public int hashCode() {
    return this.getName().hashCode();
  }

  protected void setRequired(final boolean required) {
    this.required = required;
  }
  
  protected void setUseMajorityAnnotation(final boolean useMajorityAnnotation) {
    this.useMajorityAnnotation = useMajorityAnnotation;
  }
  
  protected void setTitle(final String title) {
    this.title = title;
  }
  
  protected void setDescription(final String description) {
    this.description = description;
  }
  
  protected void addCssIncludes(final String... cssIncludes) {
    for (final String cssInclude : cssIncludes) {
      if (cssInclude == null) {
        throw new NullPointerException("Include must not be null");
      }
      if (!cssInclude.isEmpty()) { this.cssIncludes.add(cssInclude); }
    }
  }
  
  protected void addJsIncludes(final String... jsIncludes) {
    for (final String jsInclude : jsIncludes) {
      if (jsInclude == null) {
        throw new NullPointerException("Include must not be null");
      }
      if (!jsInclude.isEmpty()) { this.jsIncludes.add(jsInclude); }
    }
  }
  
  protected void addJsInitFunctions(final String... jsInitFunctions) {
    for (final String jsInitFunction : jsInitFunctions) {
      if (jsInitFunction == null) {
        throw new NullPointerException("Init function must not be null");
      }
      if (!jsInitFunction.isEmpty()) {
        this.jsInitFunctionNames.add(jsInitFunction);
      }
    }
  }
  
  protected void addUnit(final Unit unit) {
    if (unit == null) {
      throw new NullPointerException("Units must not be null");
    }
    this.units.add(unit);
  }
  
  public abstract void printHtmlInPanel(final PrintWriter output);
  
  public ComponentState createNewState() {
    final ComponentState state = this.createNewStateInstance();

    if (this.useMajorityAnnotation) {
      // Set majority annotation as default
      for (final Unit unit : this) {
        if (unit.getMajorityAnnotation() != null) {
          state.updateValue(unit.getKey(), unit.getMajorityAnnotation());
        }
      }
    }
    
    return state;
  }
  
  protected abstract ComponentState createNewStateInstance();


  
  public static class Unit {
    
    private String key;
    
    private String majorityAnnotation;
    
    private Map<String, List<String>> annotatorsByLabels;
    
    private int maxAnnotationsForLabel;
    
    private int numAnnotations;
    
    public Unit(final String key) {
      if (key == null) {
        throw new NullPointerException("The key must not be null");
      }
      this.key = key;
      this.majorityAnnotation = null;
      this.annotatorsByLabels = new HashMap<>();
      this.maxAnnotationsForLabel = 0;
      this.numAnnotations = 0;
    }
    
    public String getKey() {
      return this.key;
    }
    
    public String getMajorityAnnotation() {
      return this.majorityAnnotation;
    }
    
    public Set<String> getLabels() {
      return Collections.unmodifiableSet(this.annotatorsByLabels.keySet());
    }
    
    public List<String> getAnnotators(final String labelName) {
      final List<String> annotators = this.annotatorsByLabels.get(labelName);
      if (annotators == null) {
        return null;
      } else {
        return Collections.unmodifiableList(annotators);
      }
    }
    
    private void addAnnotation(final String annotator, final String labelName) {
      List<String> annotators = this.annotatorsByLabels.get(labelName);
      if (annotators == null) {
        annotators = new ArrayList<>();
        this.annotatorsByLabels.put(labelName, annotators);
      }
      
      annotators.add(annotator);
      if (annotators.size() == this.maxAnnotationsForLabel) {
        this.majorityAnnotation = null;
      } else if (annotators.size() > this.maxAnnotationsForLabel) {
        this.maxAnnotationsForLabel = annotators.size();
        this.majorityAnnotation = labelName;
      }

      ++this.numAnnotations;
    }
    
    private void checkMajorityAnnotation() {
      if (this.maxAnnotationsForLabel
          < ((double) this.numAnnotations) / 2.0) {
        this.majorityAnnotation = null;
      }
    }

  }

}
