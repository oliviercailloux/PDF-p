package io.github.oliviercailloux.pdf_number_pages.utils;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class PdfUtils {
	public static PDRectangle asRectangle(BBox source) {
		final BBPoint min = source.getMin();
		final BBPoint max = source.getMax();
		return new PDRectangle(min.getX(), min.getY(), max.getX() - min.getX(), max.getY() - min.getY());
	}

	public static BoundingBox getCopy(BoundingBox source) {
		return new BoundingBox(source.getLowerLeftX(), source.getLowerLeftY(), source.getUpperRightX(),
				source.getUpperRightY());
	}
}
