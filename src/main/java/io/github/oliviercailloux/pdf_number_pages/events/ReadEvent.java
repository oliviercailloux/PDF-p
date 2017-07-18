package io.github.oliviercailloux.pdf_number_pages.events;

import static java.util.Objects.requireNonNull;

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

}
