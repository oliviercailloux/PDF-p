package io.github.oliviercailloux.pdf_number_pages.pdfbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.SortedSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;

public class LabelRangesOperator {
	/**
	 * not <code>null</code>
	 */
	private String errorMessage;

	private boolean succeeded;

	public String getErrorMessage() {
		return errorMessage;
	}

	public LabelRangesByIndex read(PDPageLabels labels) {
		final LabelRangesByIndex ranges = new LabelRangesByIndex();
		final SortedSet<Integer> indices = labels.getPageIndices();
		for (Integer index : indices) {
			final PDPageLabelRange range = labels.getPageLabelRange(index);
			ranges.put(index, range);
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
				assert !document.isEncrypted();
				labelRangesByIndex.addToDocument(document);
				// document.getDocumentCatalog().setDocumentOutline(outline);
				document.save(outputPath.toFile());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public boolean succeeded() {
		return succeeded;
	}

}
