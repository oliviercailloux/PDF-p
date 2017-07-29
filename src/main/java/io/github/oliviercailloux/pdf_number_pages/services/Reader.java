package io.github.oliviercailloux.pdf_number_pages.services;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;

public class Reader {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);

	/**
	 * Not <code>null</code>, not empty.
	 */
	private Path inputPath;

	private LabelRangesByIndex labelRangesByIndex;

	private final LabelRangesOperator labelRangesOperator = new LabelRangesOperator();

	private ReadEvent lastReadEvent;

	private Outline outline;

	final EventBus eventBus = new EventBus(Reader.class.getCanonicalName());

	public Reader() {
		inputPath = Paths.get(System.getProperty("user.home"), "in.pdf");
		labelRangesByIndex = null;
		outline = null;
		lastReadEvent = null;
	}

	public Path getInputPath() {
		return inputPath;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	/**
	 * @return <code>null</code> iff no read has ever occurred.
	 */
	public LabelRangesByIndex getLastRead() {
		return labelRangesOperator.getLastRead();
	}

	public Optional<ReadEvent> getLastReadEvent() {
		return Optional.ofNullable(lastReadEvent);
	}

	public Optional<Outline> getOutline() {
		return Optional.ofNullable(outline);
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
		labelRangesByIndex.clear();
		final LabelRangesByIndex readLabelRanges = labelRangesOperator.readLabelRanges(this.inputPath);
		labelRangesByIndex.putAll(readLabelRanges);
		errorMessage = labelRangesOperator.getErrorMessage();
		succeeded = labelRangesOperator.succeeded();
		final boolean outlineReadSucceeded = labelRangesOperator.outlineReadSucceeded();
		outline.clear();
		if (outlineReadSucceeded) {
			outline.addAll(labelRangesOperator.getOutline().get().getChildren());
		}
		lastReadEvent = new ReadEvent(succeeded, errorMessage, outlineReadSucceeded,
				labelRangesOperator.getOutlineErrorMessage());
		eventBus.post(lastReadEvent);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutline(Outline outline) {
		this.outline = outline;
	}
}
