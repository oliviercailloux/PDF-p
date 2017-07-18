package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.AutoSaveChanged;
import io.github.oliviercailloux.pdf_number_pages.events.DisplayDisposedEvent;
import io.github.oliviercailloux.pdf_number_pages.events.FinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.events.InputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OutputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.events.ShellClosedEvent;
import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.LabelRangesComponent;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.ModelOperation;
import io.github.oliviercailloux.pdf_number_pages.model.PDPageLabelRangeWithEquals;
import io.github.oliviercailloux.pdf_number_pages.services.LabelRangesOperator;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaveJob;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

public class Controller {
	final private static Controller INSTANCE = new Controller();

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	public static Controller getInstance() {
		return INSTANCE;
	}

	public static void main(String[] args) throws Exception {
		final Controller app = Controller.getInstance();
//		app.createLabelsAndOutline();
		app.proceed();
	}

	private Display display;

	private final InputOutputComponent inputOutputComponent;

	private final LabelRangesByIndex labelRangesByIndex;

	private final LabelRangesComponent labelRangesComponent;

	private final LabelRangesOperator labelRangesOperator = new LabelRangesOperator();

	private final SaveOptionsComponent saveOptionsComponent;

	private Shell shell;

	final EventBus eventBus = new EventBus(Controller.class.getCanonicalName());

	final Saver saver;

	public Controller() {
		labelRangesByIndex = new LabelRangesByIndex();
		display = Display.getDefault();
		labelRangesComponent = new LabelRangesComponent();
		inputOutputComponent = new InputOutputComponent();
		saver = new Saver();
		saver.setController(this);
		saveOptionsComponent = new SaveOptionsComponent();
		final Exitter exitter = new Exitter();
		exitter.setController(this);

		register(this);
		register(inputOutputComponent);
		register(saveOptionsComponent);
		register(labelRangesComponent);
		register(saver);
		register(exitter);
	}

	@Subscribe
	public void autoSaveChanged(@SuppressWarnings("unused") AutoSaveChanged event) {
		setSaveButtonEnabled();
	}

	public void createLabels() {
		{
			final PDPageLabelRangeWithEquals r = new PDPageLabelRangeWithEquals();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_ROMAN_LOWER);
			labelRangesByIndex.put(0, r);
		}
		{
			final PDPageLabelRangeWithEquals r = new PDPageLabelRangeWithEquals();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			labelRangesByIndex.put(15, r);
		}
		{
			final PDPageLabelRangeWithEquals r = new PDPageLabelRangeWithEquals();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			labelRangesByIndex.put(25, r);
		}
		{
			final PDPageLabelRangeWithEquals r = new PDPageLabelRangeWithEquals();
			r.setStart(1);
			r.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			labelRangesByIndex.put(35, r);
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
//		final Path inputPath = inputOutputComponent.getInputPath().get();
//		final Path outputPath = inputOutputComponent.getOutputPath();
//		labelRangesOperator.setOverwrite(true);
//		labelRangesOperator.saveLabelRanges(inputPath, outputPath, labelRangesByIndex);
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

	public boolean getAutoSave() {
		return saveOptionsComponent.getAutoSave();
	}

	public InputOutputComponent getInputOutputComponent() {
		return requireNonNull(inputOutputComponent);
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public boolean getOverwrite() {
		return saveOptionsComponent.getOverwrite();
	}

	public Saver getSaver() {
		return saver;
	}

	public Shell getShell() {
		return requireNonNull(shell);
	}

	public void initGui() {
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Pdf number pages");

		labelRangesComponent.init(shell);
		saveOptionsComponent.init(shell);
		inputOutputComponent.init(shell);

		Display.getDefault().disposeExec(() -> {
			eventBus.post(new DisplayDisposedEvent());
		});

		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				eventBus.post(new ShellClosedEvent(e));
			}
		});
	}

	@Subscribe
	public void inputPathChanged(InputPathChanged event) {
		setSaveButtonEnabled();

		LOGGER.info("Input path changed, reading.");
		/**
		 * TODO We currently read when input path is changed! Actually we should
		 * do nothing until read is explicitly asked. (Huh? Check that this
		 * makes sense.)
		 */
		final String errorMessage;
		final Path inputPath = event.getInputPath();
		final boolean succeeded;
		labelRangesByIndex.clear();
		final LabelRangesByIndex readLabelRanges = labelRangesOperator.readLabelRanges(inputPath);
		labelRangesByIndex.putAll(readLabelRanges);
		errorMessage = labelRangesOperator.getErrorMessage();
		succeeded = labelRangesOperator.succeeded();

		assert Display.findDisplay(Thread.currentThread()) != null;
		eventBus.post(new ReadEvent(succeeded, errorMessage));
		eventBus.post(new ModelChanged(ModelOperation.ALL));
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		final Optional<FinishedEvent> lastSaveJobResult = saver.getLastFinishedJobResult();
		LOGGER.debug("Model changed: {}.", event);
		setSavedStatus(lastSaveJobResult);
		/**
		 * TODO here we set the saved status, and possibly it is saved already,
		 * but we might save anyway! Example: edit a combo box but do not change
		 * it, when auto save: the file is saved again. Secondly, when clicking
		 * auto save, no save should occur if file was saved already.
		 */
		setSaveButtonEnabled();
	}

	@Subscribe
	public void outputPathChanged(@SuppressWarnings("unused") OutputPathChanged event) {
		setSaveButtonEnabled();
	}

	public void proceed() {
		LOGGER.info("Start init.");
		initGui();
		inputOutputComponent
				.setInputPath(Paths.get("/home/olivier/Local/Biblio - backup/Roman - Advanced Linear Algebra.pdf"));
		display.asyncExec(() -> {
			saveOptionsComponent.setOverwrite(true);
		});
		LOGGER.info("Finished init.");
		fireView();
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
		inputOutputComponent.register(listener);
		saveOptionsComponent.register(listener);
		saver.register(listener);
		labelRangesByIndex.register(listener);
	}

	@Subscribe
	public void saverFinishedEvent(@SuppressWarnings("unused") FinishedEvent event) {
		setSavedStatus(Optional.of(event));
		inputOutputComponent.setChangesEnabled(true);
	}

	private void setSaveButtonEnabled() {
		saveOptionsComponent.setSaveButtonEnabled(!getAutoSave() && !labelRangesByIndex.isEmpty());
	}

	private void setSavedStatus(Optional<FinishedEvent> saveJobResult) {
		if (!saveJobResult.isPresent()) {
			inputOutputComponent.setSavedStatus(false);
		} else {
			final FinishedEvent event = saveJobResult.get();
			final SaveJob job = event.getSaveJob();
			LOGGER.debug("Testing equality.");
			final boolean eqModel = job.getLabelRangesByIndex().equals(labelRangesByIndex);
			LOGGER.debug("Tested equality.");
			final boolean eqInp = job.getInputPath().equals(inputOutputComponent.getInputPath());
			final boolean eqOutp = job.getOutputPath().equals(inputOutputComponent.getOutputPath());
			final boolean noErr = event.getErrorMessage().isEmpty();
			inputOutputComponent.setSavedStatus(eqModel && eqInp && eqOutp && noErr);
		}
	}

}
