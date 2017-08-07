package io.github.oliviercailloux.pdf_number_pages.services;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.SortedSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
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
import io.github.oliviercailloux.pdf_number_pages.services.saver.OutlineToPdf;

public class LabelRangesOperator {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelRangesOperator.class);

	private PDDocument current;

	/**
	 * not <code>null</code>
	 */
	private String errorMessage;

	private boolean lastOutlineReadSucceeded;

	private Outline outline;

	private String outlineErrorMessage;

	private final OutlineToPdf outlineToPdf;

	private boolean overwrite;

	private LabelRangesByIndex ranges;

	private boolean succeeded;

	public LabelRangesOperator() {
		errorMessage = "";
		overwrite = false;
		succeeded = false;
		ranges = null;
		outline = null;
		outlineToPdf = new OutlineToPdf();
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

	public void save(Path inputPath, Path outputPath, LabelRangesByIndex labelRangesByIndex) {
		assert inputPath != null;
		assert outputPath != null;
		assert labelRangesByIndex != null;
		assert labelRangesByIndex.size() >= 1;
		final File inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			errorMessage = "File not found";
			succeeded = false;
		} else {
			try (PDDocument document = PDDocument.load(inputPath.toFile())) {
				if (document.isEncrypted()) {
					errorMessage = "Document is encrypted.";
					succeeded = false;
				}
				labelRangesByIndex.addToDocument(document);
				if (outline != null) {
					outlineToPdf.setDocument(document);
					final PDDocumentOutline pdDocumentOutline = outlineToPdf.asDocumentOutline(outline);
					document.getDocumentCatalog().setDocumentOutline(pdDocumentOutline);
				}
				final StandardOpenOption[] openOptions = overwrite
						? new StandardOpenOption[] { StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE,
								StandardOpenOption.WRITE }
						: new StandardOpenOption[] { StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE };
				final Path empty = Paths.get("");
				if (outputPath.equals(empty)) {
					LOGGER.info("Output path is empty.");
				}
				try (OutputStream outStr = Files.newOutputStream(outputPath, openOptions)) {
					document.save(outStr);
				}
				errorMessage = "";
				succeeded = true;
			} catch (ClosedByInterruptException e) {
				errorMessage = "Interrupted.";
				LOGGER.debug("Writing.", e);
			} catch (FileAlreadyExistsException e) {
				errorMessage = "Already exists: " + e.getMessage();
				LOGGER.debug("Writing.", e);
			} catch (IOException e) {
				errorMessage = e.getMessage() + " (" + e.getClass().getSimpleName() + ")";
				LOGGER.error("Writing.", e);
				succeeded = false;
			}
		}
	}

	/**
	 * @param outline
	 *            <code>null</code> for no outline to save.
	 */
	public void setOutline(Outline outline) {
		this.outline = outline;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
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
		for (PDOutlineItem child : children) {
			readOutline.addAsLastChild(readOutline(child));
		}
		return readOutline;
	}

}
