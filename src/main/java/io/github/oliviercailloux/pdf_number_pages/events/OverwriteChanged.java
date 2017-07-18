package io.github.oliviercailloux.pdf_number_pages.events;

public class OverwriteChanged {

	private boolean overwrite;

	public OverwriteChanged(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean overwrite() {
		return overwrite;
	}

}
