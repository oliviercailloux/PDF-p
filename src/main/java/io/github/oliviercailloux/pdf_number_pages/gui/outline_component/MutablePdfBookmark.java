package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;

public class MutablePdfBookmark {

	private OutlineNode owner;

	/**
	 * @param owner
	 *            assumed to be non empty; may <em>not</em> become empty while used
	 *            by this object.
	 */
	public MutablePdfBookmark(OutlineNode owner) {
		this.owner = requireNonNull(owner);
		checkState(owner.getBookmark().isPresent());
	}

	public int getPhysicalPageNumber() {
		return owner.getBookmark().get().getPhysicalPageNumber();
	}

	public String getTitle() {
		return owner.getBookmark().get().getTitle();
	}

	public void setPhysicalPageNumber(int pageNumber) {
		owner.setBookmark(new PdfBookmark(getTitle(), pageNumber));
	}

	public void setTitle(String title) {
		owner.setBookmark(new PdfBookmark(title, getPhysicalPageNumber()));
	}

}
