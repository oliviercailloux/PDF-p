package io.github.oliviercailloux.pdf_number_pages.services;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaveJob;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaverFinishedEvent;

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
 * <p>
 * This object also gives the changed status, compared to the last read.
 * </p>
 *
 * @author Olivier Cailloux
 *
 */
public class StatusComputer {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusComputer.class);

	private boolean hasChanged;

	private boolean isSaved;

	private LabelRangesByIndex labelRangesByIndex;

	private Optional<Outline> outline;

	private Reader reader;

	private Saver saver;

	final EventBus eventBus = new EventBus(StatusComputer.class.getCanonicalName());

	public StatusComputer() {
		isSaved = false;
		labelRangesByIndex = null;
		saver = null;
		reader = null;
		outline = Optional.empty();
		hasChanged = false;
	}

	public Reader getReader() {
		return reader;
	}

	public Saver getSaver() {
		return saver;
	}

	/**
	 * @return <code>false</code> if no read has occurred yet.
	 */
	public boolean hasChangedSinceLastRead() {
		return hasChanged;
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
		}
		setChangedStatus();
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	@Subscribe
	public void saverFinished(@SuppressWarnings("unused") SaverFinishedEvent event) {
		setSavedStatus(event);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
		labelRangesByIndex.register(this);
	}

	public void setOutline(Optional<Outline> outline) {
		this.outline = requireNonNull(outline);
		if (outline.isPresent()) {
			outline.get().register(this);
		}
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
		saver.register(this);
	}

	private void setChangedStatus() {
		checkState(outline.isPresent());
		final Optional<ReadEvent> lastReadEvent = reader.getLastReadEvent();
		if (!lastReadEvent.isPresent()) {
			return;
		}
		final ReadEvent readEvent = lastReadEvent.get();
		final boolean labelsChanged = !readEvent.getLabelRangesByIndex().equals(labelRangesByIndex);
		final boolean outlineChanged = !readEvent.getOutline().equals(outline.get());
		hasChanged = labelsChanged || outlineChanged;
	}

	private void setSavedStatus(SaverFinishedEvent event) {
		final boolean wasSaved = isSaved;
		final SaveJob job = event.getSaveJob();
		final boolean eqRanges = job.getLabelRangesByIndex().equals(labelRangesByIndex);
		final boolean eqOutlines = job.getOutline().equals(outline);
		final boolean eqInp = job.getInputPath().equals(reader.getInputPath());
		final boolean eqOutp = job.getOutputPath().equals(saver.getOutputPath());
		final boolean noErr = event.getErrorMessage().isEmpty();
		isSaved = eqRanges && eqOutlines && eqInp && eqOutp && noErr;
		LOGGER.debug("Tested equality: {}, {}, {}, {}, {}.", eqRanges, eqOutlines, eqInp, eqOutp, noErr);

		if (wasSaved != isSaved) {
			eventBus.post(new SavedStatusChanged(isSaved));
		}
	}
}
