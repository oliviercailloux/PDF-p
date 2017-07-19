package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import io.github.oliviercailloux.pdf_number_pages.events.OutputPathChanged;
import io.github.oliviercailloux.pdf_number_pages.events.OverwriteChanged;
import io.github.oliviercailloux.pdf_number_pages.events.SaverFinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.gui.Controller;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;

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

	private final ListeningExecutorService executor = MoreExecutors
			.listeningDecorator(Executors.newSingleThreadExecutor());

	private LabelRangesByIndex labelRangesByIndex;

	/**
	 * Not <code>null</code>, not empty.
	 */
	private Path outputPath;

	private boolean overwrite;

	private Reader reader;

	private ListenableFuture<Void> submittedJob;

	final EventBus eventBus = new EventBus(Saver.class.getCanonicalName());

	Optional<SaverFinishedEvent> lastSaveJobResult;

	public Saver() {
		submittedJob = null;
		lastSaveJobResult = Optional.empty();
		outputPath = Paths.get(System.getProperty("user.home"), "out.pdf");
		labelRangesByIndex = null;
		overwrite = false;
	}

	public void close() {
		checkState(submittedJob == null || submittedJob.isDone());
		executor.shutdown();
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
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

	public void register(Object listener) {
		eventBus.register(requireNonNull(listener));
	}

	public void save() {
		checkState(!labelRangesByIndex.isEmpty());
		if (submittedJob != null) {
			submittedJob.cancel(true);
		}
		LOGGER.debug("Attempting save.");
		assert !labelRangesByIndex.isEmpty();
		final Path inputPath = reader.getInputPath();
		final SaveJob saveJob = new SaveJob(labelRangesByIndex, inputPath, outputPath, overwrite);
		submittedJob = executor.submit(new SaverRunnable(saveJob));
		Futures.addCallback(submittedJob, new FutureCallback<Void>() {

			@Override
			public void onFailure(Throwable t) {
				LOGGER.error("Problem while saving.", t);
				Display.getDefault().asyncExec(() -> {
					final SaverFinishedEvent event = new SaverFinishedEvent(saveJob, t.getMessage());
					LOGGER.debug("Saving new finished event: {}.", event);
					lastSaveJobResult = Optional.of(event);
					eventBus.post(event);
				});
			}

			@Override
			public void onSuccess(Void result) {
				final SaverFinishedEvent event = new SaverFinishedEvent(saveJob, "");
				LOGGER.debug("Saving new finished event: {}.", event);
				lastSaveJobResult = Optional.of(event);
				Display.getDefault().asyncExec(() -> eventBus.post(event));
			}
		}, MoreExecutors.directExecutor());
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutputPath(Path outputPath) {
		this.outputPath = requireNonNull(outputPath);
		LOGGER.debug("Posting output path changed event.");
		eventBus.post(new OutputPathChanged(outputPath));
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
		eventBus.post(new OverwriteChanged(overwrite));
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}
}
