package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
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

	private LabelRangesByIndex labelRangesByIndex;

	private Reader reader;

	private Saver saver;

	final EventBus eventBus = new EventBus(AutoSaver.class.getCanonicalName());

	public AutoSaver() {
		autoSave = false;
		reader = null;
		saver = null;
	}

	public boolean autoSaves() {
		return autoSave;
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

	@Subscribe
	public void inputPathChanged(@SuppressWarnings("unused") InputPathChanged event) {
		LOGGER.info("Input path changed, disabling auto save.");
		setAutoSave(false);
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
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
			checkState(!labelRangesByIndex.isEmpty());
		}
		final boolean wasAuto = this.autoSave;
		this.autoSave = autoSave;
		if (wasAuto != autoSave) {
			eventBus.post(new AutoSaveChanged(autoSave));
			savePerhaps();
		}
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
		labelRangesByIndex.register(this);
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
			assert !labelRangesByIndex.isEmpty();
			saver.save();
		}
	}
}
