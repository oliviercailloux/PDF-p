package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

public class ReadEvent {

	private String errorMessage;

	private String errorMessageOutline;

	private boolean outlineReadSucceeded;

	private boolean succeeded;

	public ReadEvent(boolean succeeded, String errorMessage, boolean outlineReadSucceeded, String errorMessageOutline) {
		this.succeeded = succeeded;
		this.errorMessage = requireNonNull(errorMessage);
		this.outlineReadSucceeded = outlineReadSucceeded;
		this.errorMessageOutline = requireNonNull(errorMessageOutline);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorMessageOutline() {
		return errorMessageOutline;
	}

	public boolean outlineReadSucceeded() {
		return outlineReadSucceeded;
	}

	public boolean succeeded() {
		return succeeded;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Succeeded", succeeded).add("Error message", errorMessage)
				.add("Outline read succeeded", outlineReadSucceeded).add("Error message outline", errorMessageOutline)
				.toString();
	}

}
