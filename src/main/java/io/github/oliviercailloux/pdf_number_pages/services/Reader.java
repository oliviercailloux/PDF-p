package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import io.github.oliviercailloux.pdf_number_pages.events.InputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;

public class Reader {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

	/**
	 * Not <code>null</code>, not empty.
	 */
	private Path inputPath;

	private LabelRangesByIndex labelRangesByIndex;

	private final LabelRangesOperator labelRangesOperator = new LabelRangesOperator();

	final EventBus eventBus = new EventBus(Reader.class.getCanonicalName());

	public Reader() {
		inputPath = Paths.get(System.getProperty("user.home"), "in.pdf");
		labelRangesByIndex = null;
	}

	public Path getInputPath() {
		return inputPath;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void setInputPath(Path inputPath) {
		this.inputPath = requireNonNull(inputPath);
		LOGGER.info("Posting input path changed event.");
		eventBus.post(new InputPathChanged(this.inputPath));

		LOGGER.info("Input path changed, reading.");
		final String errorMessage;
		final boolean succeeded;
		labelRangesByIndex.clear();
		final LabelRangesByIndex readLabelRanges = labelRangesOperator.readLabelRanges(this.inputPath);
		labelRangesByIndex.putAll(readLabelRanges);
		errorMessage = labelRangesOperator.getErrorMessage();
		succeeded = labelRangesOperator.succeeded();

		eventBus.post(new ReadEvent(succeeded, errorMessage));
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}
}
