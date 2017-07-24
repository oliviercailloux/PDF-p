package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

public class SaverFinishedEvent {

	private final String errorMessage;

	private final SaveJob saveJob;

	/**
	 * @param errorMessage
	 *            empty string iff no error has occurred.
	 */
	public SaverFinishedEvent(SaveJob saveJob, String errorMessage) {
		this.saveJob = requireNonNull(saveJob);
		this.errorMessage = requireNonNull(errorMessage);
	}

	/**
	 * @return empty string iff no error has occurred.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return not <code>null</code>.
	 */
	public SaveJob getSaveJob() {
		return saveJob;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("save job", saveJob).add("error message", errorMessage).toString();
	}
}