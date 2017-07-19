package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.SaverFinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaveJob;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

/**
 * <p>
 * Can be used to check whether the changes have been saved before quit.
 * </p>
 * <p>
 * This object gives the saved status as compared to the last time a save action
 * finished. Thus, if a save action is currently ongoing, the status given by
 * this object may be incorrect.
 * </p>
 * <p>
 * This object does not monitor the output file: if an external process changes
 * the output file (for example, the end-user deletes it) after a save action,
 * this object might report a status “saved” although the file is in fact
 * modified.
 * </p>
 *
 * @author Olivier Cailloux
 *
 */
public class SavedStatusComputer {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SavedStatusComputer.class);

	private boolean isSaved;

	private LabelRangesByIndex labelRangesByIndex;

	private Reader reader;

	private Saver saver;

	public SavedStatusComputer() {
		isSaved = false;
		labelRangesByIndex = null;
		saver = null;
		reader = null;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Reader getReader() {
		return reader;
	}

	public Saver getSaver() {
		return saver;
	}

	public boolean isSaved() {
		return isSaved;
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		final Optional<SaverFinishedEvent> lastSaveJobResult = saver.getLastFinishedJobResult();
		LOGGER.debug("Model changed: {}.", event);
		if (lastSaveJobResult.isPresent()) {
			setSavedStatus(lastSaveJobResult.get());
			/**
			 * TODO here we set the saved status, and possibly it is saved
			 * already, but we might save anyway! Example: edit a combo box but
			 * do not change it, when auto save: the file is saved again.
			 * Secondly, when clicking auto save, no save should occur if file
			 * was saved already.
			 */
		}
	}

	@Subscribe
	public void saverFinished(@SuppressWarnings("unused") SaverFinishedEvent event) {
		setSavedStatus(event);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
		labelRangesByIndex.register(this);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
		saver.register(this);
	}

	private void setSavedStatus(SaverFinishedEvent event) {
		final SaveJob job = event.getSaveJob();
		LOGGER.debug("Testing equality.");
		final boolean eqModel = job.getLabelRangesByIndex().equals(labelRangesByIndex);
		LOGGER.debug("Tested equality.");
		final boolean eqInp = job.getInputPath().equals(reader.getInputPath());
		final boolean eqOutp = job.getOutputPath().equals(saver.getOutputPath());
		final boolean noErr = event.getErrorMessage().isEmpty();
		isSaved = eqModel && eqInp && eqOutp && noErr;
	}
}
