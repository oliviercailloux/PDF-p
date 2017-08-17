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
import io.github.oliviercailloux.swt_tools.IntEditingSupport;

public class EditingSupportPage extends IntEditingSupport<OutlineNode> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportPage.class);

	private LabelRangesByIndex labelRangesByIndex;

	private Outline outline;

	public EditingSupportPage(ColumnViewer viewer) {
		super(viewer, OutlineNode.class);
		/**
		 * TODO what if page value bigger than number of pages? What if the constraint
		 * is ok now but wrong later?
		 */
		setIntegerValidator(intValue -> intValue < 1 ? "Not a page." : null);
		labelRangesByIndex = null;
		outline = null;
	}

	@Override
	public int getIntValue(OutlineNode element) {
		assert element != null;
		LOGGER.debug("Getting value for: {}.", element);
		final Optional<PdfBookmark> bookmarkOpt = element.getBookmark();
		checkState(bookmarkOpt.isPresent());
		final PdfBookmark bookmark = bookmarkOpt.get();
		return bookmark.getPhysicalPageNumber() + 1;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Outline getOutline() {
		return outline;
	}

	@Override
	public void setIntValue(OutlineNode element, int value) {
		assert element != null;
		final Optional<PdfBookmark> bookmarkOpt = element.getBookmark();
		checkState(bookmarkOpt.isPresent());
		final PdfBookmark bookmark = bookmarkOpt.get();
		element.setBookmark(new PdfBookmark(bookmark.getTitle(), value - 1));
		LOGGER.debug("Element set: {}.", element);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutline(Outline outline) {
		this.outline = requireNonNull(outline);
	}

}
