package io.github.oliviercailloux.pdf_number_pages.services.saver;

public class SaveFailedException extends Exception {

	/**
	 * @param detail
	 *            may be <code>null</code>.
	 */
	public SaveFailedException(String detail) {
		super(detail);
	}

}
