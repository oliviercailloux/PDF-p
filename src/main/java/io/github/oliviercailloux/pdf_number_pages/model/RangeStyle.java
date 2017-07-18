package io.github.oliviercailloux.pdf_number_pages.model;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public enum RangeStyle {
	DECIMAL, LOWER, NONE, ROMAN_LOWER, ROMAN_UPPER, UPPER;
	static final private BiMap<RangeStyle, String> RANGE_STYLE_PDFBOX_NAMES = ImmutableBiMap.of(RangeStyle.DECIMAL,
			PDPageLabelRange.STYLE_DECIMAL, RangeStyle.LOWER, PDPageLabelRange.STYLE_LETTERS_LOWER, RangeStyle.UPPER,
			PDPageLabelRange.STYLE_LETTERS_UPPER, RangeStyle.ROMAN_LOWER, PDPageLabelRange.STYLE_ROMAN_LOWER,
			RangeStyle.ROMAN_UPPER, PDPageLabelRange.STYLE_ROMAN_UPPER);

	static public RangeStyle fromPdfBoxStyle(String rangeStylePdfBox) {
		if (rangeStylePdfBox == null) {
			return RangeStyle.NONE;
		}
		final RangeStyle rangeStyle = RANGE_STYLE_PDFBOX_NAMES.inverse().get(rangeStylePdfBox);
		if (rangeStyle == null) {
			throw new IllegalArgumentException();
		}
		return rangeStyle;
	}

	public String toPdfBoxStyle() {
		if (this == RangeStyle.NONE) {
			return null;
		}
		final String styleName = RANGE_STYLE_PDFBOX_NAMES.get(this);
		if (styleName == null) {
			throw new IllegalStateException();
		}
		return styleName;
	}

	@Override
	public String toString() {
		switch (this) {
		case NONE:
			return "No numbering";
		case DECIMAL:
			return "Decimal";
		case LOWER:
			return "Lower letters";
		case UPPER:
			return "Upper letters";
		case ROMAN_LOWER:
			return "Roman lower letters";
		case ROMAN_UPPER:
			return "Roman upper letters";
		default:
			throw new IllegalStateException();
		}
	}
}
