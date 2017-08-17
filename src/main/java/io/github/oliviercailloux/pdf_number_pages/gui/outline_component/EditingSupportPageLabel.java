package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;
import io.github.oliviercailloux.pdf_number_pages.services.PdfPageLabelComputer;
import io.github.oliviercailloux.swt_tools.TextEditingSupport;

public class EditingSupportPageLabel extends TextEditingSupport<OutlineNode> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportPageLabel.class);

	private LabelRangesByIndex labelRangesByIndex;

	private Outline outline;

	private final PdfPageLabelComputer pdfPageLabelComputer = new PdfPageLabelComputer();

	public EditingSupportPageLabel(ColumnViewer viewer) {
		super(viewer, OutlineNode.class);
		labelRangesByIndex = null;
		outline = null;
	}

	@Override
	public boolean canEditTyped(OutlineNode element) {
		return false;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Outline getOutline() {
		return outline;
	}

	@Override
	public String getValueTyped(OutlineNode element) {
		assert element != null;
		checkState(!labelRangesByIndex.isEmpty());
		LOGGER.debug("Getting value for: {}.", element);
		final Optional<PdfBookmark> bookmarkOpt = element.getBookmark();
		checkState(bookmarkOpt.isPresent());
		final PdfBookmark bookmark = bookmarkOpt.get();
		return pdfPageLabelComputer.getLabelFromPageIndex(bookmark.getPhysicalPageNumber(), labelRangesByIndex);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutline(Outline outline) {
		this.outline = requireNonNull(outline);
	}

	@Override
	public void setValueTyped(OutlineNode element, String value) {
		throw new UnsupportedOperationException("Not implemented: set value.");
	}

}
