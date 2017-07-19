package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static java.util.Objects.requireNonNull;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.AutoSaveChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OutputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OverwriteChanged;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;

/**
 * Users must ensure that auto save is disabled whenever the model (things to be
 * saved) is empty.
 *
 * @author Olivier Cailloux
 *
 */
public class AutoSaver {
	private boolean autoSave;

	private LabelRangesByIndex labelRangesByIndex;

	private Saver saver;

	final EventBus eventBus = new EventBus(AutoSaver.class.getCanonicalName());

	public AutoSaver() {
		autoSave = false;
		saver = null;
	}

	public boolean autoSaves() {
		return autoSave;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Saver getSaver() {
		return saver;
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
		this.autoSave = autoSave;
		eventBus.post(new AutoSaveChanged(autoSave));
		savePerhaps();
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
		labelRangesByIndex.register(this);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
		saver.register(this);
	}

	private void savePerhaps() {
		/**
		 * Here the model (things to be saved) should not be empty, by design.
		 */
		if (autoSave) {
			saver.save();
		}
	}
}
