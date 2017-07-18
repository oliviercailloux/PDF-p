package io.github.oliviercailloux.pdf_number_pages.gui;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.AutoSaveChanged;
import io.github.oliviercailloux.pdf_number_pages.events.FinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.events.InputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OutputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OverwriteChanged;
import io.github.oliviercailloux.pdf_number_pages.events.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

public class SaveOptionsComponent {

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(SaveOptionsComponent.class);

	private Label label;

	private Button saveButton;

	Button autoSaveButton;

	final EventBus eventBus = new EventBus(SaveOptionsComponent.class.getCanonicalName());

	Button overwriteButton;

	Saver saver;

	public SaveOptionsComponent() {
		label = null;
		overwriteButton = null;
		saveButton = null;
		autoSaveButton = null;
		saver = null;
	}

	public void autoSaveChangedEvent(AutoSaveChanged event) {
		autoSaveButton.setSelection(event.autoSave());
	}

	public Saver getSaver() {
		return saver;
	}

	public void init(Shell shell) {
		final Composite buttonsComposite = new Composite(shell, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buttonsComposite.setLayout(new GridLayout(4, false));

		autoSaveButton = new Button(buttonsComposite, SWT.CHECK);
		autoSaveButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		autoSaveButton.setText("Auto save");
		autoSaveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LOGGER.debug("Selected button autoSave.");
				eventBus.post(new AutoSaveChanged(autoSaveButton.getSelection()));
			}
		});
		autoSaveButton.setSelection(false);

		saveButton = new Button(buttonsComposite, SWT.NONE);
		saveButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LOGGER.debug("Selected button save.");
				saver.save();
			}
		});

		overwriteButton = new Button(buttonsComposite, SWT.CHECK);
		overwriteButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		overwriteButton.setText("Overwrite");
		overwriteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saver.setOverwrite(overwriteButton.getSelection());
			}
		});
		overwriteButton.setSelection(false);

		label = new Label(buttonsComposite, SWT.NONE);
		final GridData labelLayoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		labelLayoutData.horizontalIndent = 10;
		label.setLayoutData(labelLayoutData);
		Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		label.setForeground(red);

		eventBus.post(new AutoSaveChanged(autoSaveButton.getSelection()));
	}

	@Subscribe
	public void inputPathChangedEvent(@SuppressWarnings("unused") InputPathChanged event) {
		setReadError("Reading…");
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		setSaveError("");
	}

	@Subscribe
	public void outputPathChangedEvent(@SuppressWarnings("unused") OutputPathChanged event) {
		setSaveError("");
	}

	public void overwriteChangedEvent(OverwriteChanged event) {
		Display.getCurrent().asyncExec(() -> {
			overwriteButton.setEnabled(event.overwrite());
		});
	}

	@Subscribe
	public void readEvent(ReadEvent event) {
		setReadError(event.getErrorMessage());
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	@Subscribe
	public void saverHasFinishedEvent(FinishedEvent event) {
		setSaveError(event.getErrorMessage());
	}

	public void setSaveButtonEnabled(boolean enabled) {
		checkState(saveButton != null);
		saveButton.setEnabled(enabled);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
	}

	/**
	 * @param error
	 *            empty string for no error.
	 */
	private void setReadError(String error) {
		requireNonNull(error);
		label.setText(error);
		label.requestLayout();
		LOGGER.debug("Error when reading: {}.", error);
	}

	/**
	 * @param error
	 *            empty string for no error.
	 */
	private void setSaveError(String error) {
		requireNonNull(error);
		label.setText(error);
		label.requestLayout();
		LOGGER.debug("Error when saving: {}.", error);
	}
}
