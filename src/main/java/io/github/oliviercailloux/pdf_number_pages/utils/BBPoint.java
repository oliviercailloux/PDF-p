package io.github.oliviercailloux.pdf_number_pages.utils;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Immutable. Represent a bounding box point, with the semantic that y values
 * increase upwards.
 *
 * Uses floats for compatibility with {@link PDRectangle}and the like.
 *
 * @author Olivier Cailloux
 *
 */
public class BBPoint {
	private float x;

	private float y;

	public BBPoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}
