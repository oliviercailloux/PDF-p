package io.github.oliviercailloux.pdf_number_pages.services.saver;

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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.utils.BBox;
import io.github.oliviercailloux.pdf_number_pages.utils.PdfUtils;

public class PdfSaver {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(PdfSaver.class);

	/**
	 * not <code>null</code>
	 */
	private String errorMessage;

	private final OutlineToPdf outlineToPdf;

	private boolean succeeded;

	public PdfSaver() {
		errorMessage = "";
		succeeded = false;
		outlineToPdf = new OutlineToPdf();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void save(SaveJob job) {
		assert job != null;
		final Path inputPath = job.getInputPath();
		final Path outputPath = job.getOutputPath();
		final Optional<Outline> outlineOpt = job.getOutline();
		final Optional<BBox> cropBoxOpt = job.getCropBox();
		final boolean overwrite = job.getOverwrite();
		final LabelRangesByIndex labelRangesByIndex = job.getLabelRangesByIndex();
		assert labelRangesByIndex.size() >= 1;
		final File inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			errorMessage = "File not found";
			succeeded = false;
		} else {
			try (PDDocument document = PDDocument.load(inputPath.toFile())) {
				if (cropBoxOpt.isPresent()) {
					final BBox cropBox = cropBoxOpt.get();
					final PDPageTree pages = document.getPages();
					for (PDPage page : pages) {
						page.setCropBox(PdfUtils.asRectangle(cropBox));
					}
				}

				if (document.isEncrypted()) {
					errorMessage = "Document is encrypted.";
					succeeded = false;
				}
				labelRangesByIndex.addToDocument(document);
				if (outlineOpt.isPresent()) {
					outlineToPdf.setDocument(document);
					final Outline outline = outlineOpt.get();
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

	public boolean succeeded() {
		return succeeded;
	}

}
