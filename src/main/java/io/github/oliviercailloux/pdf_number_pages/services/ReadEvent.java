package io.github.oliviercailloux.pdf_number_pages.services;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;

/**
 * Immutable.
 *
 * @author Olivier Cailloux
 *
 */
public class ReadEvent {

	private String errorMessage;

	private String errorMessageOutline;

	private LabelRangesByIndex labelRangesByIndex;

	/**
	 * May be <code>null</code>.
	 */
	private Outline outline;

	private boolean outlineReadSucceeded;

	private boolean succeeded;

	public ReadEvent(LabelRangesByIndex labelRangesByIndex, Outline outline, boolean succeeded, String errorMessage,
			boolean outlineReadSucceeded, String errorMessageOutline) {
		requireNonNull(labelRangesByIndex);
		checkArgument(!labelRangesByIndex.isEmpty());
		this.succeeded = succeeded;
		this.errorMessage = requireNonNull(errorMessage);
		this.outlineReadSucceeded = outlineReadSucceeded;
		this.errorMessageOutline = requireNonNull(errorMessageOutline);
		this.labelRangesByIndex = LabelRangesByIndex.deepImmutableCopy(labelRangesByIndex);
		requireNonNull(outline);
		this.outline = new Outline();
		this.outline.addCopies(outline.getChildren());
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorMessageOutline() {
		return errorMessageOutline;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Outline getOutline() {
		return outline;
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
