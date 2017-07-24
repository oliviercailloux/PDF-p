package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

public class ReadEvent {

	private String errorMessage;

	private boolean succeeded;

	public ReadEvent(boolean succeeded, String errorMessage) {
		this.succeeded = succeeded;
		this.errorMessage = requireNonNull(errorMessage);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public boolean succeeded() {
		return succeeded;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Succeeded", succeeded).add("Error message", errorMessage)
				.toString();
	}

}
