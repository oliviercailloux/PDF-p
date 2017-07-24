package io.github.oliviercailloux.pdf_number_pages.services.saver;

import com.google.common.base.MoreObjects;

public class OverwriteChanged {

	private boolean overwrite;

	public OverwriteChanged(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean overwrite() {
		return overwrite;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Overwrite", overwrite).toString();
	}

}
