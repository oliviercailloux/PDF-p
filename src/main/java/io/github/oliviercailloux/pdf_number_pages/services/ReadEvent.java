package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.PdfPart;

/**
 * Immutable.
 *
 * @author Olivier Cailloux
 *
 */
public class ReadEvent {

	private String errorMessage;

	private String errorMessageOutline;

	private boolean outlineReadSucceeded;

	private final PdfPart pdf;

	private boolean succeeded;

	public ReadEvent(LabelRangesByIndex labelRangesByIndex, Outline outline, boolean succeeded, String errorMessage,
			boolean outlineReadSucceeded, String errorMessageOutline) {
		pdf = new PdfPart();
		requireNonNull(labelRangesByIndex);
		this.succeeded = succeeded;
		this.errorMessage = requireNonNull(errorMessage);
		this.outlineReadSucceeded = outlineReadSucceeded;
		this.errorMessageOutline = requireNonNull(errorMessageOutline);
		pdf.setLabelRangesByIndex(LabelRangesByIndex.deepImmutableCopy(labelRangesByIndex));
		requireNonNull(outline);
		final Outline newOutline = new Outline();
		newOutline.addCopies(outline.getChildren());
		pdf.setOutline(newOutline);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorMessageOutline() {
		return errorMessageOutline;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return pdf.getLabelRangesByIndex();
	}

	public Outline getOutline() {
		return pdf.getOutline().get();
	}

	public PdfPart getPdf() {
		return pdf;
	}

	public boolean outlineReadSucceeded() {
		return outlineReadSucceeded;
	}

	public boolean succeeded() {
		return succeeded;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Succeeded", succeeded).add("Error message", errorMessage)
				.add("Outline read succeeded", outlineReadSucceeded).add("Error message outline", errorMessageOutline)
				.toString();
	}

}
