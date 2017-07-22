package io.github.oliviercailloux.pdf_number_pages.gui;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.Future;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.events.SaverFinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.events.ShellClosedEvent;
import io.github.oliviercailloux.pdf_number_pages.services.SavedStatusComputer;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

public class Exitter {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Exitter.class);

	private Dialog dialog;

	private SavedStatusComputer savedStatusComputer;

	private Saver saver;

	private Shell shell;

	private boolean waitingToClose;

	public Exitter() {
		waitingToClose = false;
		dialog = null;
		saver = null;
	}

	public void confirmThenDispose() {
		final Optional<SaverFinishedEvent> job = saver.getLastFinishedJobResult();
		checkState(job.isPresent());
		final String message;
		if (!job.isPresent() || job.get().getErrorMessage().isEmpty()) {
			message = "Not saved yet! Quit anyway?";
		} else {
			message = "Not saved: last save failed. Quit anyway?";
		}
		final boolean confirm = MessageDialog.openConfirm(shell, "Confirm", message);
		if (confirm) {
			shell.dispose();
		}
	}

	public SavedStatusComputer getSavedStatusComputer() {
		return savedStatusComputer;
	}

	public Saver getSaver() {
		return saver;
	}

	public Shell getShell() {
		return shell;
	}

	@Subscribe
	public void saverHasFinishedEvent(SaverFinishedEvent event) {
		if (!waitingToClose) {
			return;
		}
		final boolean disposed = dialog.getShell() == null || dialog.getShell().isDisposed();
		checkState(!disposed);
		dialog.close();

		final String lastErrorMessage = event.getErrorMessage();
		final boolean lastSuccess = lastErrorMessage.isEmpty();
		if (lastSuccess) {
			shell.dispose();
		} else {
			confirmThenDispose();
		}
	}

	public void setSavedStatusComputer(SavedStatusComputer savedStatusComputer) {
		this.savedStatusComputer = requireNonNull(savedStatusComputer);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	@Subscribe
	public void shellClosed(ShellClosedEvent event) {
		final Optional<Future<Void>> job = saver.getLastJobResult();
		final boolean ongoing = job.isPresent() && !job.get().isDone();
		if (ongoing) {
			event.getShellEvent().doit = false;
			waitForSave();
		} else if (!savedStatusComputer.isSaved()) {
			event.getShellEvent().doit = false;
			confirmThenDispose();
		}
	}

	public void waitForSave() {
		dialog = new MessageDialog(shell, "Saving", null, "Currently saving, please wait.", MessageDialog.WARNING, 0,
				"Cancel");
		dialog.setBlockOnOpen(false);
		LOGGER.info("Opening wait dialog.");
		waitingToClose = true;
		dialog.open();
		dialog.getShell().addDisposeListener((e) -> {
			waitingToClose = false;
		});
		LOGGER.info("Disposed: {}.", dialog.getShell().isDisposed());
	}
}
