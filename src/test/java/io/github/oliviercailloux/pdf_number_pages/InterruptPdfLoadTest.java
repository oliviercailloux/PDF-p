package io.github.oliviercailloux.pdf_number_pages;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptPdfLoadTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(InterruptPdfLoadTest.class);

	@Test
	public void cancelThread() throws Exception {
		LOGGER.info("Submitting.");
		final Thread t = new Thread(this::loadPdf);
		t.start();
		LOGGER.info("Sleeping.");
		sleep(500);
		LOGGER.info("Interrupting.");
		t.interrupt();
		LOGGER.info("Sleeping.");
		sleep(3000);
	}

	private void loadPdf() {
		LOGGER.info("Loading.");
		Path inputPath = Paths.get("in.pdf");
		try (PDDocument document = PDDocument.load(inputPath.toFile())) {
			LOGGER.info("Finishing.");
		} catch (Exception e) {
			LOGGER.error("Err while reading.", e);
		}
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}
