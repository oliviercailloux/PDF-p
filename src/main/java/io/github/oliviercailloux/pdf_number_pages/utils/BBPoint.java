package io.github.oliviercailloux.pdf_number_pages.utils;

/**
 * Immutable. Represent a bounding box point, with the semantic that y values
 * increase upwards.
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
