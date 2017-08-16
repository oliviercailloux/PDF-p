package io.github.oliviercailloux.pdf_number_pages.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.github.oliviercailloux.pdf_number_pages.model.RangeStyle;

public class TestPdfPageLabelComputer {

	@Test
	public void test() {
		final PdfPageLabelComputer comp = new PdfPageLabelComputer();
		assertEquals("", comp.getLabel(0, RangeStyle.NONE));
		assertEquals("", comp.getLabel(10, RangeStyle.NONE));
		assertEquals("", comp.getLabel(-10, RangeStyle.NONE));
		assertEquals("0", comp.getLabel(0, RangeStyle.DECIMAL));
		assertEquals("1", comp.getLabel(1, RangeStyle.DECIMAL));
		assertEquals("-1", comp.getLabel(-1, RangeStyle.DECIMAL));
		assertEquals("a", comp.getLabel(1, RangeStyle.LOWER));
		assertEquals("b", comp.getLabel(2, RangeStyle.LOWER));
		assertEquals("aa", comp.getLabel(27, RangeStyle.LOWER));
		assertEquals("A", comp.getLabel(1, RangeStyle.UPPER));
		assertEquals("B", comp.getLabel(2, RangeStyle.UPPER));
		assertEquals("AA", comp.getLabel(27, RangeStyle.UPPER));
		assertEquals("i", comp.getLabel(1, RangeStyle.ROMAN_LOWER));
		assertEquals("ii", comp.getLabel(2, RangeStyle.ROMAN_LOWER));
		assertEquals("xxvii", comp.getLabel(27, RangeStyle.ROMAN_LOWER));
		assertEquals("I", comp.getLabel(1, RangeStyle.ROMAN_UPPER));
		assertEquals("II", comp.getLabel(2, RangeStyle.ROMAN_UPPER));
		assertEquals("XXVII", comp.getLabel(27, RangeStyle.ROMAN_UPPER));
	}

}
