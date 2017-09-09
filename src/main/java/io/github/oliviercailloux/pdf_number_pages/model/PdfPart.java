package io.github.oliviercailloux.pdf_number_pages.model;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

/**
 *
 * TODO This object (and the sub-objects such as the label ranges) should
 * directly read through (and modify) the original pdf document, it would be
 * much simpler.
 *
 * @author Olivier Cailloux
 *
 */
public class PdfPart {

	private BoundingBoxKeeper boundingBoxKeeper;

	private LabelRangesByIndex labelRangesByIndex;

	private Optional<Outline> outline;

	public PdfPart() {
		labelRangesByIndex = new LabelRangesByIndex();
		outline = Optional.empty();
		boundingBoxKeeper = new BoundingBoxKeeper();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PdfPart)) {
			return false;
		}
		final PdfPart p2 = (PdfPart) obj;
		return labelRangesByIndex.equals(p2.getLabelRangesByIndex()) && outline.equals(p2.getOutline())
				&& boundingBoxKeeper.equals(p2.getBoundingBoxKeeper());
	}

	public BoundingBoxKeeper getBoundingBoxKeeper() {
		return boundingBoxKeeper;
	}

	public Optional<BBox> getCropBox() {
		return boundingBoxKeeper.getCropBox();
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Optional<Outline> getOutline() {
		return outline;
	}

	@Override
	public int hashCode() {
		return Objects.hash(labelRangesByIndex, outline, boundingBoxKeeper);
	}

	public void register(Object listener) {
		labelRangesByIndex.register(requireNonNull(listener));
		if (outline.isPresent()) {
			outline.get().register(listener);
		}
		boundingBoxKeeper.register(listener);
	}

	public void setBoundingBoxKeeper(BoundingBoxKeeper boundingBoxKeeper) {
		this.boundingBoxKeeper = requireNonNull(boundingBoxKeeper);
	}

	public void setCropBox(BBox cropBox) {
		boundingBoxKeeper.setCropBox(cropBox);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutline(Outline outline) {
		this.outline = Optional.of(outline);
	}

	@Override
	public String toString() {
		final ToStringHelper helper = MoreObjects.toStringHelper(this);
		helper.add("Labels", labelRangesByIndex);
		helper.add("Outline", outline);
		helper.add("Bounding box keeper", boundingBoxKeeper);
		return helper.toString();
	}
}
