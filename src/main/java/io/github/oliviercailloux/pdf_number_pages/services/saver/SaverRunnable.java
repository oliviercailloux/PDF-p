package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.services.LabelRangesOperator;

/**
 * <p>
 * If error during save, this runnable terminates.
 * </p>
 *
 * @author Olivier Cailloux
 *
 */
public class SaverRunnable implements Callable<Void> {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SaverRunnable.class);

	private SaveJob job;

	private final LabelRangesOperator labelRangesOperator = new LabelRangesOperator();

	public SaverRunnable(SaveJob job) {
		this.job = requireNonNull(job);
	}

	@Override
	public Void call() throws Exception {
		LOGGER.debug("Proceeding to save: {}.", job);
		final LabelRangesByIndex labelRangesByIndex = job.getLabelRangesByIndex();
		final Path inputPath = job.getInputPath();
		final Path outputPath = job.getOutputPath();
		labelRangesOperator.setOverwrite(job.getOverwrite());
		labelRangesOperator.setOutline(job.getOutline().orElse(null));
		labelRangesOperator.save(inputPath, outputPath, labelRangesByIndex);
		final String errorMessage = labelRangesOperator.getErrorMessage();
		if (!errorMessage.isEmpty()) {
			throw new SaveFailedException(errorMessage);
		}
		return null;
	}

}
