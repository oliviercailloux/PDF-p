package io.github.oliviercailloux.pdf_number_pages.model;

import java.util.Objects;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

public class PDPageLabelRangeWithEquals extends PDPageLabelRange {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(PDPageLabelRangeWithEquals.class);

	/**
	 * A range with null prefix and style, and start is 1
	 */
	public PDPageLabelRangeWithEquals() {
		super();
	}

	public PDPageLabelRangeWithEquals(PDPageLabelRange source) {
		setPrefix(source.getPrefix());
		setStart(source.getStart());
		setStyle(source.getStyle());
	}

	@Override
	public boolean equals(Object o2) {
		if (!(o2 instanceof PDPageLabelRange)) {
			return false;
		}
		final PDPageLabelRange r2 = (PDPageLabelRange) o2;
		LOGGER.debug("Testing equality: {} VS {}.", this, r2);
		return Objects.equals(this.getPrefix(), r2.getPrefix()) && Objects.equals(this.getStart(), r2.getStart())
				&& Objects.equals(this.getStyle(), r2.getStyle());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getPrefix(), getStart(), getStyle());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("prefix", getPrefix()).add("start", getStart())
				.add("style", getStyle()).toString();
	}
}
