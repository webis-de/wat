package de.aitools.aq.wat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import de.aitools.aq.wat.data.Annotator;
import de.aitools.aq.wat.data.Component;
import de.aitools.aq.wat.data.ComponentsFactory;
import de.aitools.aq.wat.data.Task;
import de.aitools.aq.wat.data.TaskState;
import de.aitools.aq.wat.io.PropertiesFiles;
import de.aitools.aq.wat.io.RecordsFile;

public class WatProject implements Iterable<Annotator> {
  
  private static final Logger LOGGER =
      Logger.getLogger(WatProject.class.getSimpleName());
  
  private static final String PROJECT_DIRECTORY_NAME = "project";
  
  private static final String TASKS_DIRECTORY_NAME = "tasks";
  
  private static final String TASKS_STATES_DIRECTORY_NAME = "task-states";
  
  private static final String RESULTS_DIRECTORY_NAME = "results";
  
  private static final String CONFIGURATION_FILE_NAME = "wat.conf";

  private static final String CONFIG_NAME = "name";

  private static final String CONFIG_ADMIN_PASSWORD = "admin.password";
  
  private static final String CONFIG_COMPONENTS = "components";
  
  private static final String CONFIG_ANONYMIZE_ANNOTATORS_FOR_CURATION = "curation.anonymize";

  private static final String CONFIG_ANNOTATORS_PREFIX = "annotator.";

  private static final String CONFIG_ANNOTATORS_LOGIN_NAME_SUFFIX = ".name";

  private static final String CONFIG_ANNOTATORS_PASSWORD_SUFFIX = ".password";

  private static final String CONFIG_ANNOTATORS_TASKS_SUFFIX = ".tasks";
  
  private final File mainDirectory;
  
  private String name;
  
  private String adminPassword;
  
  private Map<String, Annotator> annotators;
  
  public WatProject(final File mainDirectory) throws IOException {
    this.mainDirectory = mainDirectory;
    this.reload();
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getAdminPassword() {
    return this.adminPassword;
  }
  
  public Annotator getAnnotator(final String loginName) {
    if (loginName == null) { return null; }
    return this.annotators.get(loginName);
  }
  
  @Override
  public Iterator<Annotator> iterator() {
    return this.annotators.values().iterator();
  }
  
  public void reload() throws IOException {
    final Properties config = this.readConfig();
    this.name = config.getProperty(CONFIG_NAME);
    this.adminPassword = config.getProperty(CONFIG_ADMIN_PASSWORD);
    this.annotators = Collections.unmodifiableMap(this.loadAnnotators(config));
  }
  
  private Properties readConfig() throws IOException {
    return PropertiesFiles.load(
        new File(this.mainDirectory, CONFIGURATION_FILE_NAME));
  }
  
  private Map<String, Annotator> loadAnnotators(final Properties config)
  throws IOException {
    final Set<String> annotatorNames = this.getAnnotatorNames(config);
    final Map<String, Task> tasksMap = this.loadTasks(config, annotatorNames);
    
    final Map<String, Annotator> annotators = new TreeMap<>();
    for (final String annotatorName : annotatorNames) {
      LOGGER.fine("Loading annotator " + annotatorName);
      final String loginName = config.getProperty(CONFIG_ANNOTATORS_PREFIX
          + annotatorName + CONFIG_ANNOTATORS_LOGIN_NAME_SUFFIX);

      final String password = config.getProperty(CONFIG_ANNOTATORS_PREFIX
          + annotatorName + CONFIG_ANNOTATORS_PASSWORD_SUFFIX);
      
      final String[] taskNames = config.getProperty(CONFIG_ANNOTATORS_PREFIX
          + annotatorName + CONFIG_ANNOTATORS_TASKS_SUFFIX).split("\\s+");
      final Map<Task, TaskState> stateMap = new TreeMap<>();
      for (final String taskName : taskNames) {
        final Task task = tasksMap.get(taskName);
        final TaskState state = task.loadState(annotatorName);
        stateMap.put(task, state);
      }
      
      final Annotator annotator =
          new Annotator(annotatorName, loginName, password, stateMap);
      annotators.put(loginName, annotator);
    }
    return annotators;
  }
  
  private Set<String> getAnnotatorNames(final Properties config) {
    final Set<String> annotators = new HashSet<String>();
    for (final String property : config.stringPropertyNames()) {
      if (property.startsWith(CONFIG_ANNOTATORS_PREFIX)) {
        final int start = CONFIG_ANNOTATORS_PREFIX.length();
        final int nextSeparator = property.indexOf('.', start + 1);
        final String name = nextSeparator >= 0
            ? property.substring(start, nextSeparator)
            : property.substring(start);
        annotators.add(name);
      }
    }
    return annotators;
  }
  
  private Map<String, Task> loadTasks(
      final Properties config, final Set<String> annotatorNames)
  throws IOException {
    // load required stuff
    final ComponentsFactory componentsFactory =
        this.loadComponentsFactory(config); 
    final File tasksStatesDirectory =
        new File(this.mainDirectory, TASKS_STATES_DIRECTORY_NAME);
    
    // find all task names with assigned annotators
    final Set<String> taskNames = new HashSet<>();
    for (final String annotatorName : annotatorNames) {
      final String propertyName = CONFIG_ANNOTATORS_PREFIX
          + annotatorName + CONFIG_ANNOTATORS_TASKS_SUFFIX;
      final String[] annotatorTasks =
          config.getProperty(propertyName).split("\\s+");
      for (final String annotatorTask : annotatorTasks) {
        taskNames.add(annotatorTask);
      }
    }
    LOGGER.info("Found " + taskNames.size() + " tasks");
    
    // create task for each found task name
    final Map<String, Task> tasks = new HashMap<>();
    for (final String taskName : taskNames) {
      LOGGER.fine("Loading task " + taskName);
      final List<Component> components =
          componentsFactory.loadComponents(taskName);
      final Task task =
          new Task(taskName, components, this.name, tasksStatesDirectory);
      tasks.put(taskName, task);
    }
    return tasks;
  }
  
  public void writeResults()
  throws IOException {
    // load required stuff
    final Properties config = this.readConfig();
    final ComponentsFactory componentsFactory =
        this.loadComponentsFactory(config);
    final File tasksDirectory =
        new File(this.mainDirectory, TASKS_DIRECTORY_NAME);
    final File tasksStatesDirectory =
        new File(this.mainDirectory, TASKS_STATES_DIRECTORY_NAME);
    final File resultsDirectory =
        new File(this.mainDirectory, RESULTS_DIRECTORY_NAME);

    // create tasks
    for (final String taskName : tasksDirectory.list()) {
      final List<Component> components =
          componentsFactory.loadComponents(taskName);
      final Task task =
          new Task(taskName, components, this.name, tasksStatesDirectory);
      
      // create task results directory
      final File taskResultsDirectory = new File(resultsDirectory, taskName);
      taskResultsDirectory.mkdirs();

      // create task states
      final File taskStatesDirectory = new File(tasksStatesDirectory, taskName);
      for (final File recordFile : taskStatesDirectory.listFiles()) {
        final String annotatorName = RecordsFile.getAnnotatorName(recordFile);
        final TaskState state = task.loadState(annotatorName);

        if (state.isComplete()) {
          LOGGER.info("RESULTS   complete (writing):  "
              + taskName + "/" + annotatorName);
          // write results
          final File annotatorResultsFile =
              new File(taskResultsDirectory, annotatorName + ".txt");
          try (final BufferedWriter writer =
              new BufferedWriter(new FileWriter(annotatorResultsFile))) {
            state.writeKeyValues(writer);
          }
        } else {
          LOGGER.info("RESULTS incomplete (ignoring): "
              + taskName + "/" + annotatorName);
        }
      }
    }
  }
  
  private ComponentsFactory loadComponentsFactory(final Properties config) {
    final String componentMapping = config.getProperty(CONFIG_COMPONENTS);
    if (componentMapping == null) {
      throw new IllegalStateException("Components not set in "
          + CONFIGURATION_FILE_NAME + " using key " + CONFIG_COMPONENTS);
    }
    LOGGER.fine("Loading components factory for " + componentMapping);
    final String[] componentEntries = componentMapping.split("\\s+");
    final File projectDirectory =
        new File(this.mainDirectory, PROJECT_DIRECTORY_NAME);
    final File tasksDirectory =
        new File(this.mainDirectory, TASKS_DIRECTORY_NAME);
    final boolean anonymizeAnnotatorsForCuration = Boolean.parseBoolean(
        config.getProperty(CONFIG_ANONYMIZE_ANNOTATORS_FOR_CURATION, "true"));
    final ComponentsFactory componentsFactory = ComponentsFactory.load(
        componentEntries, projectDirectory, tasksDirectory,
        anonymizeAnnotatorsForCuration);
    return componentsFactory;
  }

}
