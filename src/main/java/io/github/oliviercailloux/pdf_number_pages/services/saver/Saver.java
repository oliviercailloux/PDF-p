package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.github.oliviercailloux.pdf_number_pages.model.PdfPart;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

/**
 * Controls a saver thread: whenever a job is in the queue, the saver thread
 * saves. This object puts jobs in the queue (erasing previous jobs still
 * enqueued if any). This object ensures that the related paths (input and
 * output) do not change while a saving job executes.
 *
 * @author Olivier Cailloux
 *
 */
public class Saver {

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(Saver.class);

	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(
			Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Saver").build()));

	/**
	 * Not <code>null</code>, not empty.
	 */
	private Path outputPath;

	private boolean overwrite;

	private PdfPart pdf;

	private Reader reader;

	/**
	 * Not <code>null</code>.
	 */
	private Executor savedEventsFiringExecutor;

	private ListenableFuture<Void> submittedJob;

	private SaverRunnableCallback submittedJobCallback;

	final EventBus eventBus = new EventBus(Saver.class.getCanonicalName());

	volatile Optional<SaverFinishedEvent> lastSaveJobResult;

	public Saver() {
		pdf = null;
		submittedJob = null;
		lastSaveJobResult = Optional.empty();
		outputPath = Paths.get(System.getProperty("user.home"), "out.pdf");
		overwrite = false;
		savedEventsFiringExecutor = MoreExecutors.directExecutor();
		submittedJobCallback = null;
	}

	public void close() {
		checkState(submittedJob == null || submittedJob.isDone());
		executor.shutdown();
	}

	public Optional<SaverFinishedEvent> getLastFinishedJobResult() {
		return lastSaveJobResult;
	}

	/**
	 * If the caller is processing a given event of the UI thread, this object’s
	 * last job result will not change concurrently.
	 *
	 * @return the last job result as an optional future.
	 */
	public Optional<Future<Void>> getLastJobResult() {
		return Optional.ofNullable(submittedJob);
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public boolean getOverwrite() {
		return overwrite;
	}

	public Reader getReader() {
		return reader;
	}

	public boolean isRunning() {
		return submittedJob != null && !submittedJob.isDone();
	}

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void save() {
		checkState(!pdf.getLabelRangesByIndex().isEmpty());
		assert (submittedJob == null) == (submittedJobCallback == null);
		if (submittedJob != null) {
			LOGGER.debug("Cancelling.");
			submittedJobCallback.cancel();
			submittedJob.cancel(true);
		}
		LOGGER.debug("Attempting save.");
		final Path inputPath = reader.getInputPath();
		final Optional<BBox> currentBox = pdf.getBoundingBoxKeeper().getCropBox();
		final SaveJob saveJob = new SaveJob(pdf.getLabelRangesByIndex(), pdf.getOutline(), currentBox, inputPath,
				outputPath, overwrite);
		submittedJob = executor.submit(new SaverRunnable(saveJob));
		submittedJobCallback = new SaverRunnableCallback(this, saveJob);
		Futures.addCallback(submittedJob, submittedJobCallback, savedEventsFiringExecutor);
		eventBus.post(new StartedSavingEvent());
	}

	public void setOutputPath(Path outputPath) {
		final Path oldOutputPath = this.outputPath;
		if (oldOutputPath.equals(outputPath)) {
			return;
		}
		this.outputPath = requireNonNull(outputPath);
		LOGGER.debug("Posting output path changed event.");
		eventBus.post(new OutputPathChanged(outputPath));
	}

	public void setOverwrite(boolean overwrite) {
		final boolean overwrited = this.overwrite;
		this.overwrite = overwrite;
		if (overwrited != overwrite) {
			final OverwriteChanged event = new OverwriteChanged(overwrite);
			LOGGER.debug("Firing: {}.", event);
			eventBus.post(event);
		}
	}

	public void setPdf(PdfPart pdf) {
		this.pdf = requireNonNull(pdf);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	public void setSavedEventsFiringExecutor(Executor savedEventsExecutor) {
		this.savedEventsFiringExecutor = requireNonNull(savedEventsExecutor);
	}

	void post(SaverFinishedEvent event) {
		eventBus.post(requireNonNull(event));
	}

	void setLastSaveJobResult(Optional<SaverFinishedEvent> lastSaveJobResult) {
		this.lastSaveJobResult = lastSaveJobResult;
	}
}
