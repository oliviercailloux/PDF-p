package io.github.oliviercailloux.pdf_number_pages.model;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.eventbus.EventBus;

import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

/**
 * This may store a cropbox, or an absence of crop box, to indicate that the
 * crop box should not be changed.
 *
 * @author Olivier Cailloux
 *
 */
public class BoundingBoxKeeper {
	/**
	 * “Expressed in default user space units”, as PdfBox says.
	 */
	private Optional<BBox> cropBox;

	final EventBus eventBus = new EventBus(BoundingBoxKeeper.class.getCanonicalName());

	public BoundingBoxKeeper() {
		cropBox = Optional.empty();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BoundingBoxKeeper)) {
			return false;
		}
		final BoundingBoxKeeper b2 = (BoundingBoxKeeper) obj;
		return Objects.equals(cropBox, b2.cropBox);
	}

	public Optional<BBox> getCropBox() {
		return cropBox;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cropBox);
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void removeCropBox() {
		this.cropBox = Optional.empty();
	}

	public void setCropBox(BBox cropBox) {
		this.cropBox = Optional.of(cropBox);
		eventBus.post(ModelChanged.newModelChangedAll());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Crop box", cropBox).toString();
	}
}
