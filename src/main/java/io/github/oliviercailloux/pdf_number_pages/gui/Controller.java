package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.LabelRangesComponent;
import io.github.oliviercailloux.pdf_number_pages.gui.outline_component.OutlineComponent;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PDPageLabelRangeWithEquals;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.pdf_number_pages.services.SavedStatusComputer;
import io.github.oliviercailloux.pdf_number_pages.services.saver.AutoSaver;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

public class Controller {
	private static final String APP_NAME = "PDF Number pages";

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		final Controller app = new Controller();
//		app.createLabelsAndOutline();
		app.proceed();
	}

	private AutoSaver autoSaver;

	private Image icon;

	private final InputOutputComponent inputOutputComponent;

	private final LabelRangesByIndex labelRangesByIndex;

	private final LabelRangesComponent labelRangesComponent;

	private final Outline outline;

	private final OutlineComponent outlineComponent;

	private Reader reader;

	private final SavedStatusComputer savedStatusComputer;

	private final SaveOptionsComponent saveOptionsComponent;

	Display display;

	final PrudentActor prudentActor;

	final Saver saver;

	Shell shell;

	public Controller() {
		labelRangesByIndex = new LabelRangesByIndex();
		outline = new Outline();

		reader = new Reader();
		reader.setLabelRangesByIndex(labelRangesByIndex);
		reader.setOutline(outline);

		saver = new Saver();
		saver.setLabelRangesByIndex(labelRangesByIndex);
		saver.setReader(reader);
		saver.setSavedEventsFiringExecutor((r) -> display.asyncExec(r));
		saver.setOutline(null);

		autoSaver = new AutoSaver();
		autoSaver.setSaver(saver);
		autoSaver.setLabelRangesByIndex(labelRangesByIndex);
		autoSaver.setReader(reader);

		savedStatusComputer = new SavedStatusComputer();
		savedStatusComputer.setLabelRangesByIndex(labelRangesByIndex);
		savedStatusComputer.setReader(reader);
		savedStatusComputer.setSaver(saver);

		Display.setAppName(APP_NAME);
		display = Display.getDefault();

		labelRangesComponent = new LabelRangesComponent();
		labelRangesComponent.setLabelRangesByIndex(labelRangesByIndex);
		inputOutputComponent = new InputOutputComponent();
		inputOutputComponent.setReader(reader);
		inputOutputComponent.setSaver(saver);
		inputOutputComponent.setSavedStatusComputer(savedStatusComputer);
		saveOptionsComponent = new SaveOptionsComponent();
		saveOptionsComponent.setLabelRangesByIndex(labelRangesByIndex);
		saveOptionsComponent.setSaver(saver);
		saveOptionsComponent.setAutoSaver(autoSaver);
		outlineComponent = new OutlineComponent();
		outlineComponent.setOutline(outline);
		outlineComponent.setReader(reader);

		prudentActor = new PrudentActor();
		prudentActor.setSaver(saver);
		prudentActor.setReader(reader);
		prudentActor.setSavedStatusComputer(savedStatusComputer);
		prudentActor.setLabelRangesByIndex(labelRangesByIndex);

		register(this);
		register(inputOutputComponent);
		register(saveOptionsComponent);
		register(labelRangesComponent);
		register(outlineComponent);
		register(prudentActor);
		icon = null;
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
		final PDDocumentOutline pdoutline = new PDDocumentOutline();
		{
			final PDOutlineItem item = new PDOutlineItem();
			pdoutline.addLast(item);
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
		try (InputStream inputStream = getClass().getResourceAsStream("icon-512-blue.png")) {
			icon = new Image(display, inputStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new GridLayout(1, false));
		shell.setText(APP_NAME);
		shell.setImage(icon);

		final SashForm sash = new SashForm(shell, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		labelRangesComponent.init(sash);
		outlineComponent.init(sash);
		saveOptionsComponent.init(shell);
		inputOutputComponent.init(shell);

		prudentActor.setShell(shell);
		inputOutputComponent.addInputPathButtonAction(() -> {
			prudentActor.setAction(() -> inputOutputComponent.askForInputFile());
			prudentActor.setActionQuestion("Change input anyway?");
			prudentActor.actPrudently();
		});

		display.disposeExec(() -> {
			saver.close();
			icon.dispose();
		});

		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent event) {
				event.doit = false;
				prudentActor.setAction(() -> display.close());
				prudentActor.setActionQuestion("Quit anyway?");
				prudentActor.actPrudently();
			}
		});
	}

	public void proceed() {
		/**
		 * FIXME Slow copmputer. Start GUI, immediately close. The close event is
		 * processed before the setOverwrite. The setoverwrite is processed just before
		 * closing, as part of the close, and fails as there’s no display any more.
		 */
		LOGGER.info("Start init.");
		initGui();
		display.asyncExec(() -> reader.setInputPath(Paths
				.get("/home/olivier/Biblio/Roman - Advanced Linear Algebra, Third edition (2008) - From Gen Lib.pdf")));
		display.asyncExec(() -> {
			if (!labelRangesByIndex.isEmpty()) {
				LOGGER.debug("Setting auto save.");
				autoSaver.setAutoSave(true);
			}
		});
		final OutlineNode child11 = OutlineNode.newOutline(new PdfBookmark("1.1", 11));
		final OutlineNode child2 = OutlineNode.newOutline(new PdfBookmark("2", 2));
		final OutlineNode child1 = OutlineNode.newOutline(new PdfBookmark("1", 1), ImmutableList.of(child11));
		final ImmutableList<OutlineNode> children = ImmutableList.of(child1, child2);
		display.asyncExec(() -> {
			outline.clear();
			outline.addAll(children);
		});
		LOGGER.info("Finished init.");
		fireView();
	}

	@Subscribe
	public void readEvent(ReadEvent event) {
		LOGGER.info("Received: {}.", event);
		if (event.outlineReadSucceeded()) {
			if (outline.isEmpty()) {
				saver.setOutline(null);
			} else {
				/**
				 * TODO save outline when correctly read (but will overwrite previous outline
				 * which might have more information, such as precise locations within pages).
				 */
				saver.setOutline(null);
			}
		} else {
			saver.setOutline(null);
		}
	}

	public void register(Object listener) {
		labelRangesByIndex.register(listener);
		outline.register(listener);
		saver.register(requireNonNull(listener));
		autoSaver.register(listener);
		reader.register(listener);
		savedStatusComputer.register(listener);
	}

}
