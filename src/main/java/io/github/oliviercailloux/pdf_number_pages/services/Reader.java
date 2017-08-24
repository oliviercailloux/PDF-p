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

	private ReadEvent lastReadEvent;

	private Outline outline;

	private final PdfReader pdfReader = new PdfReader();

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
		LOGGER.debug("Firing: {}.", event);
		eventBus.post(event);

		LOGGER.debug("Input path changed, reading.");
		final String errorMessage;
		final boolean succeeded;
		/**
		 * We must change the outline before the label ranges, because otherwise we get
		 * an outline (that will possibly try to refresh) with a label ranges content
		 * that does not match it, in particular, which might be empty, which might make
		 * the outline view unhappy if it relies on the label ranges.
		 */
		outline.clear();

		labelRangesByIndex.clear();
		final LabelRangesByIndex readLabelRanges = pdfReader.readLabelRanges(this.inputPath);
		labelRangesByIndex.putAll(readLabelRanges);
		errorMessage = pdfReader.getErrorMessage();
		succeeded = pdfReader.succeeded();
		final boolean outlineReadSucceeded = pdfReader.outlineReadSucceeded();
		if (outlineReadSucceeded) {
			final Outline readOutline = pdfReader.getOutline().get();
			outline.addCopies(readOutline.getChildren());
		}
		lastReadEvent = new ReadEvent(labelRangesByIndex, outline, succeeded, errorMessage, outlineReadSucceeded,
				pdfReader.getOutlineErrorMessage());
		eventBus.post(lastReadEvent);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutline(Outline outline) {
		this.outline = outline;
	}
}
