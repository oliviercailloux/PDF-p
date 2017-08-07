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

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.services.InputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.saver.AutoSaveChanged;
import io.github.oliviercailloux.pdf_number_pages.services.saver.AutoSaver;
import io.github.oliviercailloux.pdf_number_pages.services.saver.OutputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.services.saver.OverwriteChanged;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaverFinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.services.saver.StartedSavingEvent;

public class SaveOptionsComponent {

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(SaveOptionsComponent.class);

	private Label label;

	private LabelRangesByIndex labelRangesByIndex;

	private Button saveButton;

	Button autoSaveButton;

	AutoSaver autoSaver;

	Button overwriteButton;

	Saver saver;

	public SaveOptionsComponent() {
		label = null;
		overwriteButton = null;
		saveButton = null;
		autoSaveButton = null;
		saver = null;
		autoSaver = null;
		labelRangesByIndex = null;
	}

	@Subscribe
	public void autoSaveChanged(@SuppressWarnings("unused") AutoSaveChanged event) {
		assert Display.getCurrent() != null;
		autoSaveButton.setSelection(event.autoSave());
		setSaveButtonsEnabled();
	}

	public AutoSaver getAutoSaver() {
		return autoSaver;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
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
				autoSaver.setAutoSave(autoSaveButton.getSelection());
			}
		});
		autoSaveButton.setSelection(autoSaver.autoSaves());

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
		overwriteButton.setSelection(saver.getOverwrite());

		label = new Label(buttonsComposite, SWT.NONE);
		final GridData labelLayoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		labelLayoutData.minimumWidth = 100;
		labelLayoutData.horizontalIndent = 10;
		label.setLayoutData(labelLayoutData);

		setSaveButtonsEnabled();
	}

	@Subscribe
	public void inputPathChanged(@SuppressWarnings("unused") InputPathChanged event) {
		setReadText("Reading…", true);
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		assert Display.getCurrent() != null;
		setSaveButtonsEnabled();
	}

	@Subscribe
	public void outputPathChanged(@SuppressWarnings("unused") OutputPathChanged event) {
		assert Display.getCurrent() != null;
		setSaveText("", true);
	}

	@Subscribe
	public void overwriteChanged(OverwriteChanged event) {
		LOGGER.debug("Overwrite changed: {}", event);
		assert Display.getCurrent() != null;
		overwriteButton.setSelection(event.overwrite());
	}

	@Subscribe
	public void readEvent(ReadEvent event) {
		LOGGER.debug("Read event: {}.", event);
		setReadText(event.getErrorMessage(), event.succeeded());
	}

	@Subscribe
	public void saverFinished(SaverFinishedEvent event) {
		setSaveText(event.getErrorMessage(), event.getErrorMessage().isEmpty());
	}

	@Subscribe
	public void saverStarted(@SuppressWarnings("unused") StartedSavingEvent event) {
		setSaveText("Saving…", true);
	}

	public void setAutoSaver(AutoSaver autoSaver) {
		this.autoSaver = requireNonNull(autoSaver);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setSaveButtonsEnabled() {
		checkState(saveButton != null);
		saveButton.setEnabled(!autoSaver.autoSaves() && !labelRangesByIndex.isEmpty());
		autoSaveButton.setEnabled(!labelRangesByIndex.isEmpty());
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
	}

	/**
	 * @param text
	 *            empty string for no text.
	 * @param isAllRight
	 *            <code>false</code> for an error.
	 */
	private void setReadText(String text, boolean isAllRight) {
		LOGGER.debug("Setting text to {}.", text);
		requireNonNull(text);
		label.setText(text);
		if (isAllRight) {
			label.setForeground(null);
		} else {
			Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			label.setForeground(red);
			LOGGER.info("Error when reading: {}.", text);
		}
		label.requestLayout();
	}

	/**
	 * @param text
	 *            empty string for no text.
	 * @param isAllRight
	 *            <code>false</code> for an error.
	 */
	private void setSaveText(String text, boolean isAllRight) {
		LOGGER.debug("Setting text to {}.", text);
		requireNonNull(text);
		label.setText(text);
		if (isAllRight) {
			label.setForeground(null);
		} else {
			Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			label.setForeground(red);
			LOGGER.info("Error when saving: {}.", text);
		}
		label.requestLayout();
	}
}
