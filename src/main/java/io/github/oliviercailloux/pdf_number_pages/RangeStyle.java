package io.github.oliviercailloux.pdf_number_pages;

public enum RangeStyle {
	NONE, DECIMAL, LOWER, UPPER, ROMAN_LOWER, ROMAN_UPPER;
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
