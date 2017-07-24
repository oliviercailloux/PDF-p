package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;

public class Reader {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

	/**
	 * Not <code>null</code>, not empty.
	 */
	private Path inputPath;

	private final LabelRangesOperator labelRangesOperator = new LabelRangesOperator();

	private LabelRangesByIndex model;

	final EventBus eventBus = new EventBus(Reader.class.getCanonicalName());

	public Reader() {
		inputPath = Paths.get(System.getProperty("user.home"), "in.pdf");
		model = null;
	}

	public Path getInputPath() {
		return inputPath;
	}

	/**
	 * @return <code>null</code> iff no read has ever occurred.
	 */
	public LabelRangesByIndex getLastRead() {
		return labelRangesOperator.getLastRead();
	}

	public LabelRangesByIndex getModel() {
		return model;
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void setInputPath(Path inputPath) {
		final Path oldInputPath = this.inputPath;
		if (oldInputPath.equals(inputPath)) {
			return;
		}
		this.inputPath = requireNonNull(inputPath);
		final InputPathChanged event = new InputPathChanged(this.inputPath);
		LOGGER.info("Firing: {}.", event);
		eventBus.post(event);

		LOGGER.info("Input path changed, reading.");
		final String errorMessage;
		final boolean succeeded;
		model.clear();
		final LabelRangesByIndex readLabelRanges = labelRangesOperator.readLabelRanges(this.inputPath);
		model.putAll(readLabelRanges);
		errorMessage = labelRangesOperator.getErrorMessage();
		succeeded = labelRangesOperator.succeeded();

		eventBus.post(new ReadEvent(succeeded, errorMessage));
	}

	public void setModel(LabelRangesByIndex labelRangesByIndex) {
		this.model = requireNonNull(labelRangesByIndex);
	}
}
