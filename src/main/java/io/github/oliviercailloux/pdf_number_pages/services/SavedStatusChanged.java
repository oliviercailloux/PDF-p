package io.github.oliviercailloux.pdf_number_pages.services;

import com.google.common.base.MoreObjects;

public class SavedStatusChanged {
	private boolean savedStatus;

	public SavedStatusChanged(boolean savedStatus) {
		this.savedStatus = savedStatus;
	}

	public boolean isSaved() {
		return savedStatus;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Saved status", savedStatus).toString();
	}
}
