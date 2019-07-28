package de.aitools.aq.wat.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;

import de.aitools.aq.wat.data.Component;
import de.aitools.aq.wat.data.ComponentState;
import de.aitools.aq.wat.io.RecordsFile;

public class SegmentLabeling extends Component {

	private static final String JS_VALUE_FUNCTION_NAME = "setSegmentLabelingValue";

	private static final String VALUE_UNANNOTATED = "unannotated";

	private static final String LABEL_CONTINUED = "continued";

	private static final String CONFIG_TEXT_ENCODING = "encoding";

	private static final String DEFAULT_TEXT_ENCODING = StandardCharsets.UTF_8.name();

	private static final String CONFIG_LABEL_NAMES = "labels";

	private static final String CONFIG_LABEL_PREFIX = "label.";

	private static final String CONFIG_LABEL_DISPLAY_NAME_SUFFIX = ".name";

	private static final String CONFIG_LABEL_TITLE_SUFFIX = ".title";

	private static final String CONFIG_LABEL_SUBLABELS_SUFFIX = ".sublabels";

	private static final String CONFIG_LABEL_DISABLED_TITLE_SUFFIX = ".disabled.title";

	private static final String CONFIG_LABEL_KEY_SUFFIX = ".key";

	private static final char NO_KEY = 0;

	private List<List<Segment>> segmentParagraphs;

	private List<Label> labelsInParagraph;

	private List<Label> labelsEndOfParagraph;

	public SegmentLabeling(final String name, final File projectDirectory, final File taskDirectory,
			final Map<String, RecordsFile> recordFilesByAnnotator) throws IOException {
		super(name, projectDirectory, taskDirectory, recordFilesByAnnotator, JS_VALUE_FUNCTION_NAME);
		this.addCssIncludes("/css/segment-labeling.css");
		this.addJsIncludes("/js/segment-labeling.js");
		this.addJsInitFunctions("segmentLabelingInitialize");
	}

	@Override
	public void printHtmlInPanel(final PrintWriter output) {
		for (final List<Segment> segmentParagraph : this.segmentParagraphs) {
			output.append("<div class=\"segments-paragraph\">");
			for (final Segment segment : segmentParagraph) {
				segment.printHtml(output, this.getName());
			}
			output.append("</div>\n");
		}
	}

	@Override
	protected void loadForTask(final Properties config, final File projectDirectory, final File taskDirectory)
			throws IOException {
		this.segmentParagraphs = new ArrayList<>();
		this.labelsInParagraph = new ArrayList<>();
		this.labelsEndOfParagraph = new ArrayList<>();
		this.loadLabels(config);
		final String encoding = config.getProperty(CONFIG_TEXT_ENCODING, DEFAULT_TEXT_ENCODING);
		this.readSegments(taskDirectory, encoding);
	}

	private void loadLabels(final Properties config) {
		final List<Label> labels = this.loadLabels(config, config.getProperty(CONFIG_LABEL_NAMES, ""));

		for (final Label label : labels) {
			final String labelName = label.name;
			this.labelsInParagraph.add(label);
			if (!labelName.equals(LABEL_CONTINUED)) {
				this.labelsEndOfParagraph.add(label);
			} else {
				final String titleDisabled = config.getProperty(
						CONFIG_LABEL_PREFIX + labelName + CONFIG_LABEL_DISABLED_TITLE_SUFFIX,
						"Segments at the end of a paragraph can not be continued.");
				this.labelsEndOfParagraph.add(new Label(labelName, label.displayName, titleDisabled, NO_KEY, true));
			}
		}
	}

	protected List<Label> loadLabels(final Properties config, final String labelList) {
		if (labelList == null) {
			return null;
		}

		final List<Label> labels = new ArrayList<>();
		final String[] labelNames = labelList.split("\\s+");
		if (labelNames.length < 2) {
			throw new IllegalArgumentException(
					"Less than two labels defined with property '" + CONFIG_LABEL_NAMES + "'");
		}

		for (final String labelName : labelNames) {
			final String displayName = config
					.getProperty(CONFIG_LABEL_PREFIX + labelName + CONFIG_LABEL_DISPLAY_NAME_SUFFIX, labelName);
			final String title = config.getProperty(CONFIG_LABEL_PREFIX + labelName + CONFIG_LABEL_TITLE_SUFFIX, "");
			final String keyString = config.getProperty(CONFIG_LABEL_PREFIX + labelName + CONFIG_LABEL_KEY_SUFFIX);
			if (keyString != null && keyString.length() != 1) {
				throw new IllegalArgumentException(
						"Invalid shortcut key: " + keyString + " Shortcut keys must be 1 character");
			}
			final char key = keyString != null ? keyString.charAt(0) : NO_KEY;
			final Label label = new Label(labelName, displayName, title, key, false);

			final List<Label> subLabels = this.loadLabels(config,
					config.getProperty(CONFIG_LABEL_PREFIX + labelName + CONFIG_LABEL_SUBLABELS_SUFFIX));
			if (subLabels != null) {
				label.setSubLabels(subLabels);
			}

			labels.add(label);
		}
		return labels;
	}

	private void readSegments(final File taskDirectory, final String encoding) throws IOException {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(taskDirectory, this.getName() + ".txt")), encoding))) {
			List<Segment> paragraph = new ArrayList<>();

			String line;
			int s = 0;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					if (!paragraph.isEmpty()) {
						paragraph.get(paragraph.size() - 1).setLabels(this.labelsEndOfParagraph);
						this.segmentParagraphs.add(paragraph);
					}
					paragraph = new ArrayList<>();
				} else {
					if (!paragraph.isEmpty()) {
						paragraph.get(paragraph.size() - 1).setLabels(this.labelsInParagraph);
					}
					final Segment segment = new Segment(String.valueOf(s), line);
					paragraph.add(segment);
					this.addUnit(segment);
					++s;
				}
			}

			if (!paragraph.isEmpty()) {
				paragraph.get(paragraph.size() - 1).setLabels(this.labelsEndOfParagraph);
				this.segmentParagraphs.add(paragraph);
			}
		}
	}

	@Override
	protected ComponentState createNewStateInstance() {
		return new SegmentLabelingState(this);
	}

	private static class SegmentLabelingState extends ComponentState {

		private final int numSegments;

		private int numUnannotated;

		public SegmentLabelingState(final SegmentLabeling component) {
			this.numSegments = component.getSize();
			this.numUnannotated = this.numSegments;
		}

		@Override
		public String getProgress() {
			return (this.numSegments - this.numUnannotated) + "/" + this.numSegments + " segments";
		}

		@Override
		public boolean isComplete() {
			return this.numUnannotated == 0;
		}

		@Override
		protected void updateProgress(final String key, final String value) {
			final String oldValue = this.getValue(key);
			final boolean wasAnnotated = this.isAnnotated(oldValue);
			final boolean isAnnotated = this.isAnnotated(value);
			if (isAnnotated && !wasAnnotated) {
				--this.numUnannotated;
			} else if (!isAnnotated && wasAnnotated) {
				++this.numUnannotated;
			}
		}

		private boolean isAnnotated(final String value) {
			return value != null && !value.equals(SegmentLabeling.VALUE_UNANNOTATED);
		}

	}

	private static class Segment extends Unit {

		private String text;

		private List<Label> labels;

		private Segment(final String key, final String text) {
			super(key);
			this.setText(text.replaceAll("\\s+", " ").trim());
			this.labels = new ArrayList<>();
		}

		public void setText(final String text) {
			if (text == null) {
				throw new NullPointerException();
			}
			this.text = text;
		}

		private void setLabels(final List<Label> labels) {
			this.labels = labels;
		}

		public void printHtml(final PrintWriter output, final String componentName) {
			output.append("<span class=\"segment ").append(VALUE_UNANNOTATED).append("\" id=\"").append(componentName)
					.append(this.getKey()).append("\" title=\"").append("segment " + this.getKey()).append("\"")
					.append(" data-label=\"").append(VALUE_UNANNOTATED).append("\">");

			final int lastSpace = this.text.lastIndexOf(' ');
			if (lastSpace > 0) {
				output.append(StringEscapeUtils.escapeHtml4(this.text.substring(0, lastSpace))).append(' ');
			}
			output.append("<span class=\"nobreak\">");
			output.append(StringEscapeUtils.escapeHtml4(this.text.substring(lastSpace + 1)));

			output.append("&nbsp;<span class=\"dropdown\">")
					.append("<img class=\"dropdown-toggle\" data-toggle=\"dropdown\"/>");
			output.append("<ul class=\"dropdown-menu\" role=\"menu\">");

			for (int l = 0; l < this.labels.size(); ++l) {
				final Label label = this.labels.get(l);
				label.printHtml(output, componentName, this);
			}

			output.append("</ul>");
			output.append("</span>");

			output.append("</span>");
			output.append("</span>");
		}

	}

	public static class Label {

		protected final String name;

		protected final String displayName;

		protected final String title;

		protected final char key;

		protected final boolean isDisabled;

		protected List<Label> subLabels;

		public Label(final String name, final String displayName, final String title, final char key,
				final boolean isDisabled) {
			if (name == null) {
				throw new NullPointerException();
			}
			if (displayName == null) {
				throw new NullPointerException();
			}
			if (title == null) {
				throw new NullPointerException();
			}

			this.name = name;
			this.displayName = displayName;
			this.title = title;
			this.key = key;
			this.isDisabled = isDisabled;
			this.subLabels = null;
		}

		protected void setSubLabels(final List<Label> subLabels) {
			this.subLabels = subLabels;
		}

		public void printHtml(final PrintWriter output, final String componentName, final Segment segment) {
			final String id = "annotate-" + componentName + segment.getKey() + "-as-" + this.name;
			final List<String> annotators = segment.getAnnotators(this.name);

			output.append("<li id=\"").append(id).append("\" role=\"presentation\" class=\"").append(this.name);
			if (this.subLabels != null) {
				output.append(" dropdown-submenu");
			}
			if (this.isDisabled) {
				output.append(" disabled");
			}
			output.append("\" title=\"").append(this.title);
			if (annotators != null) {
				output.append(" | annotated by: ").append(String.join(", ", annotators));
			}
			output.append("\">");

			output.append("<a class=\"annotation-button");
			if (this.subLabels != null) {
				output.append("  dropdown-submenu-toggle");
			}
			output.append("\" role=\"menuitem\" ").append("tabindex=\"-1\" ").append("href=\"javascript:if(!$('#")
					.append(id).append("').hasClass('disabled')){update('").append(componentName).append("', ")
					.append(segment.getKey()).append(", '").append(this.name).append("')}\"");
			if (this.key != NO_KEY) {
				output.append(" data-key=\"").append(this.key).append("\"");
			}
			if (this.subLabels != null) {
				output.append(" data-submenu=\"").append(this.name).append("\"");
			}
			output.append(">");

			output.append(this.displayName);

			if (this.subLabels != null) {
				output.append("<span class=\"caret\"></span>");
			} else if (annotators != null) {
				output.append(" (" + annotators.size() + ")");
			}
			output.append("</a>");

			if (this.subLabels != null) {
				output.append("<ul class=\"dropdown-menu\" data-submenu=\"").append(this.name)
						.append("\" role=\"menu\">");
				for (final Label subLabel : this.subLabels) {
					subLabel.printHtml(output, componentName, segment);
				}
				output.append("</ul>");
			}

			output.append("</li>");
		}

	}

}
