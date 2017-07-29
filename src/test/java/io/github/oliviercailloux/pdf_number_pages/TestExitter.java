package io.github.oliviercailloux.pdf_number_pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.gui.PrudentActor;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaveJob;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaverFinishedEvent;

public class TestExitter {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(TestExitter.class);

	@Test
	public void test() throws Exception {
		final Display display = Display.getDefault();

		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Test exitter");

		final PrudentActor exitter = new PrudentActor();
		exitter.setShell(shell);
		exitter.waitForSave();
		exitter.setAction(() -> shell.dispose());
		display.timerExec(1 * 1000, () -> {
			final SaveJob saveJob = Mockito.mock(SaveJob.class);
			exitter.saverHasFinishedEvent(new SaverFinishedEvent(saveJob, ""));
		});
		shell.setSize(500, 500);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
