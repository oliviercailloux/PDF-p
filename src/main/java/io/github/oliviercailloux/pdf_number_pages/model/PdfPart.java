package io.github.oliviercailloux.pdf_number_pages.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

public class PdfPart {

	/**
	 * “Expressed in default user space units”, as PdfBox says.
	 */
	private Optional<BBox> cropBox;

	private LabelRangesByIndex labelRangesByIndex;

	private Optional<Outline> outline;

	public Optional<BBox> getCropBox() {
		return cropBox;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Optional<Outline> getOutline() {
		return outline;
	}

	public void setCropBox(BBox cropBox) {
		this.cropBox = Optional.of(cropBox);
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
		helper.add("Crop box", cropBox);
		return helper.toString();
	}
}
