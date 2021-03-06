package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineChanged;
import io.github.oliviercailloux.pdf_number_pages.model.PdfPart;
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

	private PdfPart pdf;

	private Reader reader;

	private Saver saver;

	final EventBus eventBus = new EventBus(StatusComputer.class.getCanonicalName());

	public StatusComputer() {
		pdf = null;
		isSaved = false;
		saver = null;
		reader = null;
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

	@Subscribe
	public void outlineChanged(@SuppressWarnings("unused") OutlineChanged event) {
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

	public void setPdf(PdfPart pdf) {
		this.pdf = requireNonNull(pdf);
		pdf.register(this);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
		saver.register(this);
	}

	private void setChangedStatus() {
		final Optional<ReadEvent> lastReadEventOpt = reader.getLastReadEvent();
		if (!lastReadEventOpt.isPresent()) {
			return;
		}
		final ReadEvent lastReadEvent = lastReadEventOpt.get();
		final PdfPart readPdf = lastReadEvent.getPdf();
		hasChanged = pdf.equals(readPdf);
	}

	private void setSavedStatus(SaverFinishedEvent event) {
		final boolean wasSaved = isSaved;
		final SaveJob job = event.getSaveJob();
		final PdfPart savedPdf = job.getPdf();
		final boolean eqPdf = pdf.equals(savedPdf);
		final boolean eqInp = job.getInputPath().equals(reader.getInputPath());
		final boolean eqOutp = job.getOutputPath().equals(saver.getOutputPath());
		final boolean noErr = event.getErrorMessage().isEmpty();
		isSaved = eqPdf && eqInp && eqOutp && noErr;
		LOGGER.debug("Tested equality: {}, {}, {}, {}.", eqPdf, eqInp, eqOutp, noErr);

		if (wasSaved != isSaved) {
			eventBus.post(new SavedStatusChanged(isSaved));
		}
	}
}
