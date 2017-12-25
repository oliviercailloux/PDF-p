package io.github.oliviercailloux.pdf_number_pages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptReadTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(InterruptReadTest.class);

	@Test
	public void interruptRead() throws Exception {
		LOGGER.info("Submitting.");
		final Thread t = new Thread(this::justRead);
		t.start();
		LOGGER.info("Sleeping.");
		sleep(10);
		/**
		 * On my computer, the read takes > 150 ms if not interrupted. Interrupting
		 * makes the read stop after 60 ms.
		 */
		LOGGER.info("Interrupting.");
		t.interrupt();
		LOGGER.info("Sleeping again.");
		sleep(200);
	}

	private Void justRead() {
		final Path inputPath = Paths.get("in.pdf");
		try {
			LOGGER.info("Reading.");
			Files.readAllBytes(inputPath);
			LOGGER.info("Returning.");
		} catch (IOException e) {
			LOGGER.info("Err while reading.", e);
		}
		return null;
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}
