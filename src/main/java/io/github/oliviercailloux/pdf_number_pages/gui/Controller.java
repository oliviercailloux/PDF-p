package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Paths;

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

import io.github.oliviercailloux.pdf_number_pages.events.DisplayDisposedEvent;
import io.github.oliviercailloux.pdf_number_pages.events.ShellClosedEvent;
import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.LabelRangesComponent;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.PDPageLabelRangeWithEquals;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.pdf_number_pages.services.saver.AutoSaver;
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

	private AutoSaver autoSaver;

	private Display display;

	private final InputOutputComponent inputOutputComponent;

	private final LabelRangesByIndex labelRangesByIndex;

	private final LabelRangesComponent labelRangesComponent;

	private Reader reader;

	private final SaveOptionsComponent saveOptionsComponent;

	private Shell shell;

	final EventBus eventBus = new EventBus(Controller.class.getCanonicalName());

	final Saver saver;

	public Controller() {
		labelRangesByIndex = new LabelRangesByIndex();

		saver = new Saver();
		saver.setLabelRangesByIndex(labelRangesByIndex);

		autoSaver = new AutoSaver();
		autoSaver.setSaver(saver);
		autoSaver.setLabelRangesByIndex(labelRangesByIndex);

		reader = new Reader();
		reader.setLabelRangesByIndex(labelRangesByIndex);

		display = Display.getDefault();

		labelRangesComponent = new LabelRangesComponent();
		labelRangesComponent.setLabelRangesByIndex(labelRangesByIndex);
		inputOutputComponent = new InputOutputComponent();
		inputOutputComponent.setReader(reader);
		inputOutputComponent.setSaver(saver);
		saveOptionsComponent = new SaveOptionsComponent();
		saveOptionsComponent.setLabelRangesByIndex(labelRangesByIndex);
		saveOptionsComponent.setSaver(saver);
		saveOptionsComponent.setAutoSaver(autoSaver);

		final Exitter exitter = new Exitter();
		exitter.setSaver(saver);
		exitter.setShell(shell);

		register(this);
		register(inputOutputComponent);
		register(saveOptionsComponent);
		register(labelRangesComponent);
		register(exitter);
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
		return autoSaver.autoSaves();
	}

	public InputOutputComponent getInputOutputComponent() {
		return requireNonNull(inputOutputComponent);
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
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

	public void proceed() {
		LOGGER.info("Start init.");
		initGui();
		reader.setInputPath(Paths.get("/home/olivier/Local/Biblio - backup/Roman - Advanced Linear Algebra.pdf"));
		saver.setOverwrite(true);
		LOGGER.info("Finished init.");
		fireView();
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
		inputOutputComponent.register(listener);
		saveOptionsComponent.register(listener);
		saver.register(listener);
		autoSaver.register(listener);
		reader.register(listener);
		labelRangesByIndex.register(listener);
	}

}
