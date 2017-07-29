package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/**
 * Immutable.
 *
 * @author Olivier Cailloux
 *
 */
public class PdfBookmark {
	/**
	 * ≥ 0.
	 */
	private int physicalPageNumber;

	/**
	 * Not <code>null</code>.
	 */
	private String title;

	public PdfBookmark() {
		physicalPageNumber = 0;
		title = "";
	}

	public PdfBookmark(String title, int physicalPageNumber) {
		checkArgument(physicalPageNumber >= 0);
		this.physicalPageNumber = physicalPageNumber;
		this.title = Strings.nullToEmpty(title);
	}

	@Override
	public boolean equals(Object o2) {
		if (!(o2 instanceof PdfBookmark)) {
			return false;
		}
		final PdfBookmark b2 = (PdfBookmark) o2;
		return Objects.equals(title, b2.title) && Objects.equals(physicalPageNumber, b2.physicalPageNumber);
	}

	public int getPhysicalPageNumber() {
		return physicalPageNumber;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public int hashCode() {
		return Objects.hash(title, physicalPageNumber);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Title", title).add("Page number", physicalPageNumber).toString();
	}

}
