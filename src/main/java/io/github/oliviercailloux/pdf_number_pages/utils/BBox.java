package io.github.oliviercailloux.pdf_number_pages.utils;

/**
 * Immutable.
 *
 * @author Olivier Cailloux
 *
 */
public class BBox {
	private BBPoint min, max;

	public BBox(BBPoint min, BBPoint max) {
		this.min = min;
		this.max = max;
	}

	public BBPoint getMax() {
		return max;
	}

	public BBPoint getMin() {
		return min;
	}

}
