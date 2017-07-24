package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.google.common.util.concurrent.FutureCallback;

public class SaverRunnableCallback implements FutureCallback<Void> {
	private volatile boolean proceed;

	private final SaveJob saveJob;

	private Saver saver;

	public SaverRunnableCallback(Saver saver, SaveJob saveJob) {
		this.saver = requireNonNull(saver);
		this.saveJob = requireNonNull(saveJob);
		proceed = true;
	}

	public void cancel() {
		proceed = false;
	}

	@Override
	public void onFailure(Throwable t) {
		if (!proceed) {
			return;
		}
		Saver.LOGGER.error("Problem while saving.", t);
		final SaverFinishedEvent event = new SaverFinishedEvent(saveJob, t.getMessage());
		Saver.LOGGER.debug("Saving new finished event: {}.", event);
		saver.setLastSaveJobResult(Optional.of(event));
		saver.post(event);
	}

	@Override
	public void onSuccess(Void result) {
		if (!proceed) {
			return;
		}
		final SaverFinishedEvent event = new SaverFinishedEvent(saveJob, "");
		Saver.LOGGER.debug("Saving new finished event: {}.", event);
		saver.setLastSaveJobResult(Optional.of(event));
		saver.post(event);
	}
}