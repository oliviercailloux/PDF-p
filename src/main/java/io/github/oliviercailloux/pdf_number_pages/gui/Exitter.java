package io.github.oliviercailloux.pdf_number_pages.gui;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.Future;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.FinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.events.ShellClosedEvent;

public class Exitter {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Exitter.class);

	private Controller controller;

	private Dialog dialog;

	private boolean waitingToClose;

	public Exitter() {
		waitingToClose = false;
		dialog = null;
	}

	@Subscribe
	public void saverHasFinishedEvent(FinishedEvent event) {
		if (!waitingToClose) {
			return;
		}

		dialog.close();

		final String lastErrorMessage = event.getErrorMessage();
		if (lastErrorMessage.isEmpty()) {
			controller.getShell().dispose();
		} else {
			final MessageBox dialog2 = new MessageBox(controller.getShell());
			dialog2.setText("Error while saving");
			dialog2.setMessage("Error while saving: " + lastErrorMessage + "\nClose cancelled.");
			dialog2.open();
		}
	}

	public void setController(Controller controller) {
		this.controller = requireNonNull(controller);
	}

	@Subscribe
	public void shellClosed(ShellClosedEvent event) {
		/**
		 * TODO check if possible close then press save, event save is enqueued,
		 * while waiting for close to be processed. (In which case save command
		 * will be sent after closing.)
		 */
		checkState(Display.getCurrent() != null);

		LOGGER.debug("Shell closed.");

		final Optional<Future<Void>> lastJobResult = controller.getSaver().getLastJobResult();
		if (!lastJobResult.isPresent() || lastJobResult.get().isDone()) {
			LOGGER.debug("No last job result, nothing to do.");
			/** No race condition here because we’re still in the UI thread. */
			controller.getSaver().close();
			return;
		}

		event.getShellEvent().doit = false;
		LOGGER.info("Last job not finished, displaying dialog.");
		dialog = new MessageDialog(controller.getShell(), "Saving", null, "Currently saving, please wait.",
				MessageDialog.WARNING, 0, "");
		dialog.setBlockOnOpen(false);
		dialog.open();
		waitingToClose = true;
		LOGGER.info("Exit task terminating, remain: {}.", Thread.activeCount());
	}
}
