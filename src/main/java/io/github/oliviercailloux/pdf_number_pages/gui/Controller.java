package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

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

import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.LabelRangesComponent;
import io.github.oliviercailloux.pdf_number_pages.gui.outline_component.OutlineComponent;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.PdfPart;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.pdf_number_pages.services.StatusComputer;
import io.github.oliviercailloux.pdf_number_pages.services.saver.AutoSaver;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

/**
 * TODO Consider implementing an L2Controller, not linked to the GUI (lower
 * level). It contains the model: labels, outline, and reader and saver. This
 * controller delegates to the l2controller. The l2controller is the sole
 * abilitated to change the model (possibly via reader / saver?). Also,
 * possibly: the l2controller has the data and writes to it, instead of the
 * reader and saver. Those only have read-only access to the data. The
 * l2controller fires the events. It is better able to decide when to fire
 * exactly (e.g. clear all labels and all outline and then fire).
 *
 * Start position strategy. Evolution, Libre office Writer : remembers size and
 * position. GEdit : remembers size but not pos (starts close to upper left
 * corner).
 *
 * @author Olivier Cailloux
 *
 */
public class Controller {
	private static final String APP_NAME = "PDF Number pages";

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	public static void main(String[] args) throws Exception {
		LOGGER.info("Start soft.");
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		final Controller app = new Controller();
//		app.createLabelsAndOutline();
		app.proceed();
	}

	private AutoSaver autoSaver;

	private final CropComponent cropComponent;

	private Image icon;

	private final InputOutputComponent inputOutputComponent;

	private final LabelRangesComponent labelRangesComponent;

	private final OutlineComponent outlineComponent;

	private PdfPart pdf;

	private Reader reader;

	private final SaveOptionsComponent saveOptionsComponent;

	private final StatusComputer statusComputer;

	Display display;

	final PrudentActor prudentActor;

	final Saver saver;

	Shell shell;

	public Controller() {
		pdf = new PdfPart();
		final Outline outline = new Outline();
		pdf.setOutline(outline);
		final LabelRangesByIndex labelRangesByIndex = pdf.getLabelRangesByIndex();

		reader = new Reader();
		reader.setLabelRangesByIndex(labelRangesByIndex);
		reader.setOutline(outline);

		saver = new Saver();
		saver.setPdf(pdf);
		saver.setReader(reader);
		saver.setSavedEventsFiringExecutor((r) -> display.asyncExec(r));

		autoSaver = new AutoSaver();
		autoSaver.setPdf(pdf);
		autoSaver.setReader(reader);
		autoSaver.setSaver(saver);

		statusComputer = new StatusComputer();
		statusComputer.setPdf(pdf);
		statusComputer.setReader(reader);
		statusComputer.setSaver(saver);

		Display.setAppName(APP_NAME);
		display = Display.getDefault();

		labelRangesComponent = new LabelRangesComponent();
		labelRangesComponent.setLabelRangesByIndex(labelRangesByIndex);
		inputOutputComponent = new InputOutputComponent();
		inputOutputComponent.setReader(reader);
		inputOutputComponent.setSaver(saver);
		inputOutputComponent.setStatusComputer(statusComputer);
		saveOptionsComponent = new SaveOptionsComponent();
		saveOptionsComponent.setLabelRangesByIndex(labelRangesByIndex);
		saveOptionsComponent.setSaver(saver);
		saveOptionsComponent.setAutoSaver(autoSaver);
		outlineComponent = new OutlineComponent();
		outlineComponent.setOutline(outline);
		outlineComponent.setReader(reader);
		outlineComponent.setLabelRangesByIndex(labelRangesByIndex);
		cropComponent = new CropComponent();
		cropComponent.setBoundingBoxKeeper(pdf.getBoundingBoxKeeper());

		prudentActor = new PrudentActor();
		prudentActor.setSaver(saver);
		prudentActor.setReader(reader);
		prudentActor.setStatusComputer(statusComputer);
		prudentActor.setLabelRangesByIndex(labelRangesByIndex);

		icon = null;
	}

	public void fireView() {
		shell.pack();
		shell.open();
		LOGGER.info("Shell opened.");
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
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
		cropComponent.init(shell);
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

		register(this);
		register(inputOutputComponent);
		register(saveOptionsComponent);
		register(labelRangesComponent);
		register(outlineComponent);
		register(prudentActor);
	}

	public void proceed() {
		LOGGER.info("Start proceed.");
		/**
		 * At this stage, the model and services are initialized. We change the model
		 * here. So that the change events are not processed by the GUI. When the GUI is
		 * initialized (just after), it sees the right values from the start, thus
		 * avoiding a refresh that would be visible by the end-user.
		 */
		reader.setInputPath(Paths.get(
				"/home/olivier/Biblio/Roman - Advanced Linear Algebra, Third edition (2008) - From Springer, with structure.pdf"));
		if (!pdf.getLabelRangesByIndex().isEmpty()) {
			LOGGER.debug("Setting auto save.");
			saver.setOverwrite(true);
			autoSaver.setAutoSave(true);
		}

		LOGGER.info("Start init.");
		initGui();
		LOGGER.info("Finished init.");
		fireView();
		/** TODO check how fast it starts in target env. */
	}

	public void register(Object listener) {
		pdf.getLabelRangesByIndex().register(listener);
		pdf.getOutline().get().register(listener);
		pdf.getBoundingBoxKeeper().register(listener);
		saver.register(requireNonNull(listener));
		autoSaver.register(listener);
		reader.register(listener);
		statusComputer.register(listener);
	}

}
