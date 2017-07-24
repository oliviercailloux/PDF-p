package io.github.oliviercailloux.pdf_number_pages.services.saver;

public class AutoSaveChanged {

	private boolean autoSave;

	public AutoSaveChanged(boolean autoSave) {
		this.autoSave = autoSave;
	}

	public boolean autoSave() {
		return autoSave;
	}

}
