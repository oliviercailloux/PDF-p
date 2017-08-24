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

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.pdf_number_pages.services.StatusComputer;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaverFinishedEvent;

/**
 * Executes an action after having checked that the current model is saved and
 * possibly asked user for confirmation.
 *
 * @author Olivier Cailloux
 *
 */
public class PrudentActor {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(PrudentActor.class);

	private Runnable action;

	/**
	 * Not <code>null</code>.
	 */
	private String actionQuestion;

	private Dialog dialog;

	private LabelRangesByIndex labelRangesByIndex;

	private Reader reader;

	private Saver saver;

	private Shell shell;

	private StatusComputer statusComputer;

	private boolean waitingForSave;

	public PrudentActor() {
		waitingForSave = false;
		dialog = null;
		saver = null;
		labelRangesByIndex = null;
		actionQuestion = "";
		action = null;
	}

	public void actPrudently() {
		final Optional<Future<Void>> job = saver.getLastJobResult();
		final boolean ongoing = job.isPresent() && !job.get().isDone();
		if (ongoing) {
			LOGGER.debug("Let’s act prudently. We wait.");
			waitForSave();
		} else if (!statusComputer.isSaved() && !labelRangesByIndex.isEmpty()
				&& statusComputer.hasChangedSinceLastRead()) {
			LOGGER.debug("Let’s act prudently. We confirm.");
			confirmThenAct();
		} else {
			LOGGER.debug("Let’s act not too prudently. We rush.");
			action.run();
		}
	}

	public void confirmThenAct() {
		final Optional<SaverFinishedEvent> job = saver.getLastFinishedJobResult();
		final String message;
		if (!job.isPresent() || job.get().getErrorMessage().isEmpty()) {
			message = "Not saved yet! " + actionQuestion;
		} else {
			message = "Not saved: last save failed. " + actionQuestion;
		}
		final boolean confirm = MessageDialog.openConfirm(shell, "Confirm", message);
		if (confirm) {
			action.run();
		}
	}

	public Runnable getAction() {
		return action;
	}

	public String getActionQuestion() {
		return actionQuestion;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Reader getReader() {
		return reader;
	}

	public Saver getSaver() {
		return saver;
	}

	public Shell getShell() {
		return shell;
	}

	public StatusComputer getStatusComputer() {
		return statusComputer;
	}

	@Subscribe
	public void saverHasFinishedEvent(SaverFinishedEvent event) {
		if (!waitingForSave) {
			return;
		}
		assert dialog != null;
		final boolean disposed = dialog.getShell() == null || dialog.getShell().isDisposed();
		checkState(!disposed);
		dialog.close();

		final String lastErrorMessage = event.getErrorMessage();
		final boolean lastSuccess = lastErrorMessage.isEmpty();
		if (lastSuccess) {
			action.run();
		} else {
			confirmThenAct();
		}
	}

	public void setAction(Runnable action) {
		this.action = requireNonNull(action);
	}

	public void setActionQuestion(String actionQuestion) {
		this.actionQuestion = requireNonNull(actionQuestion);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	public void setSaver(Saver saver) {
		this.saver = requireNonNull(saver);
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public void setStatusComputer(StatusComputer statusComputer) {
		this.statusComputer = requireNonNull(statusComputer);
	}

	public void waitForSave() {
		dialog = new MessageDialog(shell, "Saving", null, "Currently saving, please wait.", MessageDialog.WARNING, 0,
				"Cancel");
		dialog.setBlockOnOpen(false);
		LOGGER.info("Opening wait dialog.");
		waitingForSave = true;
		dialog.open();
		dialog.getShell().addDisposeListener((e) -> {
			waitingForSave = false;
		});
		LOGGER.debug("Dialog shell disposed: {}.", dialog.getShell().isDisposed());
	}
}
