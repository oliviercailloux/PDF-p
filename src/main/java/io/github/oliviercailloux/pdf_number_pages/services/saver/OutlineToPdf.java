package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import io.github.oliviercailloux.pdf_number_pages.model.IOutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;

public class OutlineToPdf {
	private PDDocument document;

	public OutlineToPdf() {
		document = null;
	}

	public PDDocumentOutline asDocumentOutline(IOutlineNode outline) {
		final PDDocumentOutline pdDocumentOutline = new PDDocumentOutline();
		final Iterable<OutlineNode> children = outline.getChildren();
		for (OutlineNode child : children) {
			final PDOutlineItem asOutlineItem = asOutlineItem(child);
			pdDocumentOutline.addLast(asOutlineItem);
		}
		return pdDocumentOutline;
	}

	/**
	 * @param outline
	 *            not empty.
	 * @return the equivalent PDOutlineItem, not <code>null</code>.
	 */
	public PDOutlineItem asOutlineItem(OutlineNode outline) {
		checkArgument(outline.getBookmark().isPresent());
		final PdfBookmark bookmark = outline.getBookmark().get();
		final PDOutlineItem item = asOutlineItem(bookmark);
		final Iterable<OutlineNode> children = outline.getChildren();
		for (OutlineNode child : children) {
			item.addLast(asOutlineItem(child));
		}
		return item;
	}

	public PDDocument getDocument() {
		return document;
	}

	public void setDocument(PDDocument document) {
		this.document = requireNonNull(document);
	}

	private PDOutlineItem asOutlineItem(PdfBookmark bookmark) {
		checkState(document != null);
		final PDOutlineItem item = new PDOutlineItem();
		item.setTitle(bookmark.getTitle());
		item.setDestination(document.getPage(bookmark.getPhysicalPageNumber()));
		return item;
	}
}
