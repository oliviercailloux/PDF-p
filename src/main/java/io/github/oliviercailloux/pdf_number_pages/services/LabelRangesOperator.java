package io.github.oliviercailloux.pdf_number_pages.services;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.SortedSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.PDPageLabelRangeWithEquals;

public class LabelRangesOperator {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelRangesOperator.class);

	/**
	 * not <code>null</code>
	 */
	private String errorMessage;

	private boolean overwrite;

	private boolean succeeded;

	public LabelRangesOperator() {
		errorMessage = "";
		overwrite = false;
		succeeded = false;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public LabelRangesByIndex read(PDPageLabels labels) {
		final LabelRangesByIndex ranges = new LabelRangesByIndex();
		final SortedSet<Integer> indices = labels.getPageIndices();
		for (Integer index : indices) {
			final PDPageLabelRange range = labels.getPageLabelRange(index);
			final PDPageLabelRangeWithEquals newRange = new PDPageLabelRangeWithEquals(range);
			ranges.put(index, newRange);
		}
//		for (int noPage = 0, nbRangesFound = 0; nbRangesFound < labels.getPageRangeCount(); ++noPage) {
//			final PDPageLabelRange range = labels.getPageLabelRange(noPage);
//			if (range != null) {
//				ranges.put(noPage, range);
//				++nbRangesFound;
//			}
//		}
		return ranges;
	}

	public LabelRangesByIndex readLabelRanges(Path inputPath) {
		final LabelRangesByIndex labelRangesByIndex = new LabelRangesByIndex();
		assert inputPath != null;
		final File inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			errorMessage = "File not found";
			succeeded = false;
		} else {
			try (PDDocument document = PDDocument.load(inputFile)) {
				assert !document.isEncrypted();
				final PDDocumentCatalog catalog = document.getDocumentCatalog();
				final PDPageLabels labels = catalog.getPageLabels();
				labelRangesByIndex.putAll(read(labels));
				errorMessage = "";
				succeeded = true;
			} catch (IOException e) {
				labelRangesByIndex.clear();
				LOGGER.error("Reading input file.", e);
				errorMessage = e.getMessage();
				succeeded = false;
			}
		}
		return labelRangesByIndex;
	}

	public void saveLabelRanges(Path inputPath, Path outputPath, LabelRangesByIndex labelRangesByIndex) {
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
				// document.getDocumentCatalog().setDocumentOutline(outline);
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
			} catch (IOException e) {
				if (e instanceof FileAlreadyExistsException) {
					errorMessage = "Already exists: " + e.getMessage();
					LOGGER.debug("Writing.", e);
				} else {
					errorMessage = e.getMessage();
					LOGGER.error("Writing.", e);
				}
				succeeded = false;
			}
		}
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean succeeded() {
		return succeeded;
	}

}
