package io.github.oliviercailloux.pdf_number_pages.model;

import io.github.oliviercailloux.pdf_number_pages.utils.BBPoint;
import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

public class BoundingBoxKeeper {
	private BBox cropBox = new BBox(new BBPoint(0, 0), new BBPoint(0, 0));

	public BBox getCropBox() {
		return cropBox;
	}

	public void setCropBox(BBox cropBox) {
		this.cropBox = cropBox;
	}
}
