package io.github.oliviercailloux.pdf_number_pages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import io.github.oliviercailloux.pdf_number_pages.label_ranges_component.LabelRangesComponent;
import io.github.oliviercailloux.pdf_number_pages.pdfbox.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.pdfbox.LabelRangesOperator;

public class App {
	public static final int RANGE_STYLE_DECIMAL = 1;

	final private static App INSTANCE = new App();

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static App getInstance() {
		return INSTANCE;
	}

	public static void main(String[] args) throws Exception {
		final App app = App.getInstance();
//		app.createLabelsAndOutline();
		app.proceed();
	}

	private Display display;

	private final InputOutputComponent inputOutputComponent;

	final private LabelRangesByIndex labelRangesByIndex;

	private final LabelRangesComponent labelRangesComponent;

	private final LabelRangesOperator labelRangesOperator;

	final private BiMap<RangeStyle, String> RANGE_STYLE_PDFBOX_NAMES = ImmutableBiMap.of(RangeStyle.DECIMAL,
			PDPageLabelRange.STYLE_DECIMAL, RangeStyle.LOWER, PDPageLabelRange.STYLE_LETTERS_LOWER, RangeStyle.UPPER,
			PDPageLabelRange.STYLE_LETTERS_UPPER, RangeStyle.ROMAN_LOWER, PDPageLabelRange.STYLE_ROMAN_LOWER,
			RangeStyle.ROMAN_UPPER, PDPageLabelRange.STYLE_ROMAN_UPPER);

	private boolean saveAuto;

	private Shell shell;

	public App() {
		labelRangesByIndex = new LabelRangesByIndex();
		saveAuto = true;
		display = new Display();
		labelRangesComponent = new LabelRangesComponent();
		inputOutputComponent = new InputOutputComponent();
		labelRangesOperator = new LabelRangesOperator();
	}

	public void add() {
		final int newIndex;
		if (labelRangesByIndex.isEmpty()) {
			newIndex = 1;
		} else {
			final int lastKey = labelRangesByIndex.lastKey().intValue();
			newIndex = lastKey + 1;
		}
		final PDPageLabelRange range = new PDPageLabelRange();
		range.setStyle(PDPageLabelRange.STYLE_DECIMAL);
		put(Integer.valueOf(newIndex), range);
		savePerhaps();
	}

	public void createLabels() {
		{
			final PDPageLabelRange r = new PDPageLabelRange();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_ROMAN_LOWER);
			put(0, r);
		}
		{
			final PDPageLabelRange r = new PDPageLabelRange();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			put(15, r);
		}
		{
			final PDPageLabelRange r = new PDPageLabelRange();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			put(25, r);
		}
		{
			final PDPageLabelRange r = new PDPageLabelRange();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			put(35, r);
		}
	}

	public void createLabelsAndOutline() {
		createLabels();
		final PDDocumentOutline outline = new PDDocumentOutline();
		{
			final PDOutlineItem item = new PDOutlineItem();
			outline.addLast(item);
			item.setTitle("Preface to the Second Edition");
			final PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
			dest.setPageNumber(6);
			item.setDestination(dest);
		}
		savePerhaps();
	}

	public void fireView() {
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public RangeStyle getRangeStyleFromPdfBox(String rangeStylePdfBox) {
		if (rangeStylePdfBox == null) {
			return RangeStyle.NONE;
		}
		final RangeStyle rangeStyle = RANGE_STYLE_PDFBOX_NAMES.inverse().get(rangeStylePdfBox);
		if (rangeStyle == null) {
			throw new IllegalArgumentException();
		}
		return rangeStyle;
	}

	public String getRangeStylePdfBox(RangeStyle rangeStyle) {
		if (rangeStyle == RangeStyle.NONE) {
			return null;
		}
		final String styleName = RANGE_STYLE_PDFBOX_NAMES.get(rangeStyle);
		if (styleName == null) {
			throw new IllegalArgumentException();
		}
		return styleName;
	}

	public void initGui() {
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new GridLayout(1, false));

		labelRangesComponent.init(shell);

		final Button autoSaveButton = new Button(shell, SWT.CHECK);
		autoSaveButton.setText("auto save");

		inputOutputComponent.init(shell);
	}

	public void inputPathChanged() {
		readPerhaps();
		if (!getLabelRangesByIndex().isEmpty()) {
			savePerhaps();
		}
	}

	public void move(int oldIndex, int newIndex) {
		final PDPageLabelRange old = labelRangesByIndex.remove(oldIndex);
		labelRangesComponent.getViewer().remove(oldIndex);
		final PDPageLabelRange range = old;
		assert range != null;
		labelRangesByIndex.put(newIndex, range);
		labelRangesComponent.getViewer().add(newIndex);
		LOGGER.debug("Removed {}, added {}.", oldIndex, newIndex);
		savePerhaps();
	}

	public void proceed() {
		initGui();
		inputOutputComponent.setInputPath(
				Optional.of(Paths.get("/home/olivier/Local/Biblio - backup/Roman - Advanced Linear Algebra.pdf")));
		inputPathChanged();
		fireView();
	}

	public void put(Integer index, PDPageLabelRange range) {
		labelRangesByIndex.put(index, range);
		labelRangesComponent.getViewer().add(index);
	}

	public void readPerhaps() {
		final String errorMessage;
		final Optional<Path> inputPath = inputOutputComponent.getInputPath();
		final boolean succeeded;
		labelRangesByIndex.clear();
		if (!inputPath.isPresent()) {
			errorMessage = "";
			succeeded = false;
		} else {
			final LabelRangesByIndex readLabelRanges = labelRangesOperator.readLabelRanges(inputPath.get());
			labelRangesByIndex.putAll(readLabelRanges);
			errorMessage = labelRangesOperator.getErrorMessage();
			succeeded = labelRangesOperator.succeeded();
		}

		setReadError(errorMessage);
		if (succeeded) {
			labelRangesComponent.getViewer().setInput(labelRangesByIndex.keySet());
		}
		labelRangesComponent.setKeyListenerEnabled(succeeded);
	}

	public void remove(int index) {
		assert index != 0 : "Removal at first page not supported";
		final PDPageLabelRange old = labelRangesByIndex.remove(index);
		assert old != null;
		labelRangesComponent.getViewer().remove(index);
		savePerhaps();
	}

	public void savePerhaps() {
		assert !labelRangesByIndex.isEmpty();
		assert inputOutputComponent.getInputPath().isPresent();
		if (!saveAuto) {
			return;
		}
		final Path outputPath = inputOutputComponent.getOutputPath();
		final Path inputPath = inputOutputComponent.getInputPath().get();
		labelRangesOperator.saveLabelRanges(inputPath, outputPath, labelRangesByIndex);
	}

	public void setPrefix(int elementIndex, String prefix) {
		assert prefix != null;
		final PDPageLabelRange element = getLabelRangesByIndex().get(elementIndex);
		element.setPrefix(Strings.emptyToNull(prefix));
		LOGGER.debug("Set prefix value for {}: {}.", elementIndex, prefix);
		labelRangesComponent.updatePrefix(elementIndex);
		savePerhaps();
	}

	public void setReadError(String error) {
		assert error != null;
		if (error.isEmpty()) {
			return;
		}
		LOGGER.info("Error when reading: {}.", error);
	}

	public void setSaveAuto(boolean save) {
		this.saveAuto = save;
	}

	public void setStart(int elementIndex, int start) {
		final PDPageLabelRange element = getLabelRangesByIndex().get(elementIndex);
		element.setStart(start);
		LOGGER.debug("Set start value for {}: {}.", elementIndex, start);
		labelRangesComponent.updateStart(elementIndex);
		savePerhaps();
	}

	public void setStyle(int elementIndex, RangeStyle style) {
		final PDPageLabelRange range = getLabelRangesByIndex().get(elementIndex);
		LOGGER.debug("Setting style value for {}: {}.", elementIndex, style);
		final String stylePdfBox = getRangeStylePdfBox(style);
		range.setStyle(stylePdfBox);
		labelRangesComponent.updateStyle(elementIndex);
		savePerhaps();
	}

	public void showExists() {
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("File exists - shell");
		MessageBox dialog = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_WARNING | SWT.OK);
		dialog.setText("File exists");
		dialog.setMessage("Do you really want to do this?");
		dialog.open();
	}

}
