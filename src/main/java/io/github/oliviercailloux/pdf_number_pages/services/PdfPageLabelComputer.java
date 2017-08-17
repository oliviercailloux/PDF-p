package io.github.oliviercailloux.pdf_number_pages.services;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Locale;
import java.util.Map.Entry;

import com.google.common.base.Strings;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.PDPageLabelRangeWithEquals;
import io.github.oliviercailloux.pdf_number_pages.model.RangeStyle;

public class PdfPageLabelComputer {
	/**
	 * Code copied from
	 * org.apache.pdfbox.pdmodel.common.PDPageLabels.LabelGenerator, maven
	 * coordinates org.apache.pdfbox:pdfbox:2.0.7, with minor changes.
	 *
	 * @author Igor Podolskiy (original)
	 * @author Olivier Cailloux (minor changes)
	 *
	 */
	private static class LabelGenerator {
		/**
		 * Lookup table used by the {@link #makeRomanLabel(int)} method.
		 */
		public static final String[][] ROMANS = new String[][] {
				{ "", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix" },
				{ "", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc" },
				{ "", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm" } };

		/**
		 * a..z, aa..zz, aaa..zzz ... labeling as described in PDF32000-1:2008, Table
		 * 159, Page 375.
		 */
		public static String makeLetterLabel(int num) {
			StringBuilder buf = new StringBuilder();
			int numLetters = num / 26 + Integer.signum(num % 26);
			int letter = num % 26 + 26 * (1 - Integer.signum(num % 26)) + 'a' - 1;
			for (int i = 0; i < numLetters; i++) {
				buf.appendCodePoint(letter);
			}
			return buf.toString();
		}

		public static String makeRomanLabel(int pIndex) {
			int pageIndex = pIndex;
			StringBuilder buf = new StringBuilder();
			int power = 0;
			while (power < 3 && pageIndex > 0) {
				buf.insert(0, ROMANS[power][pageIndex % 10]);
				pageIndex /= 10;
				power++;
			}
			// Prepend as many m as there are thousands (which is
			// incorrect by the roman numeral rules for numbers > 3999,
			// but is unbounded and Adobe Acrobat does it this way).
			// This code is somewhat inefficient for really big numbers,
			// but those don't occur too often (and the numbers in those cases
			// would be incomprehensible even if we and Adobe
			// used strict Roman rules).
			for (int i = 0; i < pageIndex; i++) {
				buf.insert(0, 'm');
			}
			return buf.toString();
		}
	}

	public String getLabel(int virtualIndex, RangeStyle style) {
		switch (style) {
		case NONE:
			return "";
		case LOWER:
			return LabelGenerator.makeLetterLabel(virtualIndex);
		case UPPER:
			return LabelGenerator.makeLetterLabel(virtualIndex).toUpperCase(Locale.ENGLISH);
		case DECIMAL:
			return String.valueOf(virtualIndex);
		case ROMAN_LOWER:
			return LabelGenerator.makeRomanLabel(virtualIndex);
		case ROMAN_UPPER:
			return LabelGenerator.makeRomanLabel(virtualIndex).toUpperCase(Locale.ENGLISH);
		default:
			throw new IllegalStateException();
		}
	}

	public String getLabelFromPageIndex(int index, LabelRangesByIndex labelRangesByIndex) {
		/**
		 * Example: ask for index 6 given range starting at absolute page index 4,
		 * labelling starting at 20. Should consider virtual index 20 + (6 − 4) = 22.
		 */
		final Entry<Integer, PDPageLabelRangeWithEquals> rangeEntry = labelRangesByIndex.floorEntry(index);
		checkArgument(rangeEntry != null);
		final int rangeStartAbsolute = rangeEntry.getKey();
		final PDPageLabelRangeWithEquals range = rangeEntry.getValue();
		final int offset = index - rangeStartAbsolute;
		assert offset >= 0;

		final int rangeStart = range.getStart();
		final int virtualIndex = rangeStart + offset;
		final String pdfBoxStyle = range.getStyle();
		final RangeStyle style = RangeStyle.fromPdfBoxStyle(pdfBoxStyle);
		final String computedLabel = getLabel(virtualIndex, style);
		return Strings.nullToEmpty(range.getPrefix()) + computedLabel;
	}
}
