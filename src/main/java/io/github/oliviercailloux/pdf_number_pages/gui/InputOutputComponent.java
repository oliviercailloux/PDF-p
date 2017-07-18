package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.InputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OutputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

/**
 * The UI uses the terms source and dest, but the code rather uses input and
 * output.
 *
 * @author Olivier Cailloux
 *
 */
public class InputOutputComponent {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(InputOutputComponent.class);

	private Composite composite;

	private Button destButton;

	private Label destText;

	private final EventBus eventBus = new EventBus(InputOutputComponent.class.getCanonicalName());

	private Reader reader;

	private Saver saver;

	private Button sourceButton;

	boolean enabled;

	Label sourceText;

	public InputOutputComponent(Saver saver) {
		this.saver = requireNonNull(saver);
		enabled = true;
		sourceButton = null;
		destButton = null;
		destText = null;
	}

	public void askForInputFile() {
		final FileDialog fileDialog = new FileDialog(composite.getShell());
		fileDialog.setText("Select file to open");
		fileDialog.setFilterExtensions(new String[] { "*.pdf" });
		final String toOpen = fileDialog.open();
		if (toOpen != null) {
			reader.setInputPath(Paths.get(toOpen));
		}
	}

	public void askForOutputFile() {
		final FileDialog fileDialog = new FileDialog(composite.getShell());
		fileDialog.setText("Select file to open");
		fileDialog.setFilterExtensions(new String[] { "*.pdf" });
		final String toOpen = fileDialog.open();
		if (toOpen != null) {
			saver.setOutputPath(Paths.get(toOpen));
		}
	}

	public Reader getReader() {
		return reader;
	}

	public Saver getSaver() {
		return saver;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(2, false));

		sourceButton = new Button(composite, SWT.NONE);
		sourceButton.setText("Source");
		sourceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		sourceText = new Label(composite, SWT.NONE);
		sourceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setInputText(reader.getInputPath());
		sourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (enabled) {
					askForInputFile();
				}
			}
		});

		destButton = new Button(composite, SWT.NONE);
		destButton.setText("Dest");
		destButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		destText = new Label(composite, SWT.NONE);
		destText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setOutputText(saver.getOutputPath());
		destButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (enabled) {
					askForOutputFile();
				}
			}
		});
	}

	public void inputPathChangedEvent(InputPathChanged event) {
		Display.getCurrent().asyncExec(() -> {
			setInputText(event.getInputPath());
		});
	}

	@Subscribe
	public void outputPathChangedEvent(OutputPathChanged event) {
		setOutputText(event.getOutputPath());
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void setChangesEnabled(boolean enabled) {
		this.enabled = enabled;
		if (composite.isDisposed()) {
			return;
		}
		sourceButton.setEnabled(this.enabled);
		destButton.setEnabled(this.enabled);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	public void setSavedStatus(boolean saved) {
		if (saved) {
			destText.setForeground(null);
		} else {
			Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			destText.setForeground(red);
		}
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
	}

	void setInputText(Path inputPath) {
		final String text = inputPath.toString();
		sourceText.setText(text);
	}

	void setOutputText(Path outputPath) {
		final String text = outputPath.toString();
		destText.setText(text);
	}

}
