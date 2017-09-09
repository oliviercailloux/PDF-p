package io.github.oliviercailloux.pdf_number_pages.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.SortedSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PDPageLabelRangeWithEquals;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;

public class PdfReader {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(PdfReader.class);

	private PDDocument current;

	/**
	 * not <code>null</code>
	 */
	private String errorMessage;

	private boolean lastOutlineReadSucceeded;

	private Outline outline;

	private String outlineErrorMessage;

	private LabelRangesByIndex ranges;

	private boolean succeeded;

	public PdfReader() {
		errorMessage = "";
		succeeded = false;
		ranges = null;
		outline = null;
		current = null;
		lastOutlineReadSucceeded = false;
		outlineErrorMessage = "";
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return <code>null</code> iff no read has ever occurred.
	 */
	public LabelRangesByIndex getLastRead() {
		return ranges;
	}

	public Optional<Outline> getOutline() {
		return Optional.ofNullable(outline);
	}

	public String getOutlineErrorMessage() {
		return outlineErrorMessage;
	}

	public boolean outlineReadSucceeded() {
		return lastOutlineReadSucceeded;
	}

	public LabelRangesByIndex read(PDPageLabels labels) {
		ranges = new LabelRangesByIndex();
		if (labels == null) {
			final PDPageLabelRangeWithEquals range = new PDPageLabelRangeWithEquals();
			range.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			ranges.put(0, range);
		} else {
			final SortedSet<Integer> indices = labels.getPageIndices();
			for (Integer index : indices) {
				final PDPageLabelRange range = labels.getPageLabelRange(index);
				final PDPageLabelRangeWithEquals newRange = new PDPageLabelRangeWithEquals(range);
				ranges.put(index, newRange);
			}
		}
		return ranges;
	}

	public LabelRangesByIndex readLabelRanges(Path inputPath) {
		assert inputPath != null;
		final File inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			errorMessage = "File not found";
			succeeded = false;
			ranges = new LabelRangesByIndex();
		} else {
			try (PDDocument document = PDDocument.load(inputFile)) {
				current = document;
				assert !document.isEncrypted();
				final PDDocumentCatalog catalog = document.getDocumentCatalog();
				final PDPageLabels labels = catalog.getPageLabels();
				read(labels);
				final PDPageTree pages = document.getPages();
				for (PDPage page : pages) {
					LOGGER.debug("CB: {}.", page.getCropBox());
					LOGGER.debug("AB: {}.", page.getArtBox());
					LOGGER.debug("BB: {}.", page.getBBox());
					LOGGER.debug("BlB: {}.", page.getBleedBox());
					LOGGER.debug("TB: {}.", page.getTrimBox());
					LOGGER.debug("MB: {}.", page.getMediaBox());
				}
				final PDDocumentOutline pdOutline = catalog.getDocumentOutline();
				read(pdOutline);
				errorMessage = "";
				succeeded = true;
			} catch (IOException e) {
				LOGGER.error("Reading input file.", e);
				errorMessage = e.getMessage();
				succeeded = false;
			}
		}
		return ranges;
	}

	public boolean succeeded() {
		return succeeded;
	}

	private void read(PDDocumentOutline pdOutline) throws IOException {
		outline = new Outline();
		if (pdOutline == null) {
			lastOutlineReadSucceeded = true;
			return;
		}
		final Iterable<PDOutlineItem> children = pdOutline.children();
		try {
			for (PDOutlineItem child : children) {
				outline.addAsLastChild(readOutline(child));
			}
			lastOutlineReadSucceeded = true;
		} catch (@SuppressWarnings("unused") ComplexOutlineException e) {
			outline = null;
			outlineErrorMessage = e.getMessage();
			lastOutlineReadSucceeded = false;
		}
	}

	private OutlineNode readOutline(PDOutlineItem pdOutline) throws ComplexOutlineException, IOException {
		final PDPage dest = pdOutline.findDestinationPage(current);
		if (dest == null) {
			throw new ComplexOutlineException("This outline is too complex for me.");
		}
		final int pageNb = current.getPages().indexOf(dest);
		final String title = pdOutline.getTitle();
		final PdfBookmark bookmark = new PdfBookmark(title, pageNb);
		final OutlineNode readOutline = OutlineNode.newOutline(bookmark);
		final Iterable<PDOutlineItem> children = pdOutline.children();
		LOGGER.debug("Reading children of: {}.", title);
		for (PDOutlineItem child : children) {
			readOutline.addAsLastChild(readOutline(child));
		}
		return readOutline;
	}

}
