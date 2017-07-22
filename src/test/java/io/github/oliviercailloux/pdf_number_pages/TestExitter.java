package io.github.oliviercailloux.pdf_number_pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.events.SaverFinishedEvent;
import io.github.oliviercailloux.pdf_number_pages.gui.Exitter;
import io.github.oliviercailloux.pdf_number_pages.services.saver.SaveJob;

public class TestExitter {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(TestExitter.class);

	@Test
	public void test() throws Exception {
//		Controller controller = Mockito.spy(Controller.class);
//		Saver saver = Mockito.spy(Saver.class);
//		Mockito.when(controller.getSaver()).thenReturn(saver);
//		final Future<Void> f = Mockito.mock(Future.class);
////		todo();
//		Mockito.when(saver.getLastJobResult()).thenReturn(Optional.of(f));
//		Mockito.when(f.get()).then(i -> {
//			Thread.sleep(1 * 1000);
//			return "oups";
//		}).thenReturn(null);
//		saver.setController(controller);
//		controller.proceed();
//		final Saver saver = Mockito.mock(Saver.class);
//		Mockito.when(saver.getLastFinishedJobResult())

		final Display display = Display.getDefault();

		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Test exitter");

		final Exitter exitter = new Exitter();
		exitter.setShell(shell);
		exitter.waitForSave();
		display.timerExec(3 * 1000, () -> {
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
