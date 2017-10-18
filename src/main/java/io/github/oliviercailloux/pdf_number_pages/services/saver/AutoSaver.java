package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineChanged;
import io.github.oliviercailloux.pdf_number_pages.model.PdfPart;
import io.github.oliviercailloux.pdf_number_pages.services.InputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;

/**
 * Users must ensure that auto save is disabled whenever the model (things to be
 * saved) is empty.
 *
 * @author Olivier Cailloux
 *
 */
public class AutoSaver {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AutoSaver.class);

	private boolean autoSave;

	private PdfPart pdf;

	private Reader reader;

	private Saver saver;

	final EventBus eventBus = new EventBus(AutoSaver.class.getCanonicalName());

	public AutoSaver() {
		pdf = null;
		autoSave = false;
		reader = null;
		saver = null;
	}

	public boolean autoSaves() {
		return autoSave;
	}

	public Reader getReader() {
		return reader;
	}

	public Saver getSaver() {
		return saver;
	}

	@Subscribe
	public void inputPathChanged(@SuppressWarnings("unused") InputPathChanged event) {
		LOGGER.debug("Input path changed, disabling auto save.");
		setAutoSave(false);
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		LOGGER.debug("Model changed, saving perhaps.");
		savePerhaps();
	}

	@Subscribe
	public void outlineChanged(@SuppressWarnings("unused") OutlineChanged event) {
		LOGGER.debug("Model changed, saving perhaps.");
		savePerhaps();
	}

	@Subscribe
	public void outputPathChanged(@SuppressWarnings("unused") OutputPathChanged event) {
		savePerhaps();
	}

	@Subscribe
	public void overwriteChanged(@SuppressWarnings("unused") OverwriteChanged event) {
		savePerhaps();
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void setAutoSave(boolean autoSave) {
		if (autoSave) {
			checkState(!pdf.getLabelRangesByIndex().isEmpty());
		}
		final boolean wasAuto = this.autoSave;
		this.autoSave = autoSave;
		if (wasAuto != autoSave) {
			eventBus.post(new AutoSaveChanged(autoSave));
			savePerhaps();
		}
	}

	public void setPdf(PdfPart pdf) {
		this.pdf = requireNonNull(pdf);
		pdf.register(this);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
		this.reader.register(this);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
		saver.register(this);
	}

	private void savePerhaps() {
		if (autoSave) {
			assert !pdf.getLabelRangesByIndex().isEmpty();
			saver.save();
		}
	}
}
