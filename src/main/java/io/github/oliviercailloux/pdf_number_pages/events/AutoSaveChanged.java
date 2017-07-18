package io.github.oliviercailloux.pdf_number_pages.events;

public class AutoSaveChanged {

	private boolean autoSave;

	public AutoSaveChanged(boolean autoSave) {
		this.autoSave = autoSave;
	}

	public boolean autoSave() {
		return autoSave;
	}

}
