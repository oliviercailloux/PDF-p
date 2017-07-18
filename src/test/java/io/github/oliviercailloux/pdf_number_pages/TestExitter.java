package io.github.oliviercailloux.pdf_number_pages;

import java.util.Optional;
import java.util.concurrent.Future;

import org.junit.Test;
import org.mockito.Mockito;

import io.github.oliviercailloux.pdf_number_pages.gui.Controller;
import io.github.oliviercailloux.pdf_number_pages.services.saver.Saver;

public class TestExitter {

	@Test
	public void test() throws Exception {
		Controller controller = Mockito.spy(Controller.class);
		Saver saver = Mockito.spy(Saver.class);
		Mockito.when(controller.getSaver()).thenReturn(saver);
		final Future<Void> f = Mockito.mock(Future.class);
//		todo();
		Mockito.when(saver.getLastJobResult()).thenReturn(Optional.of(f));
		Mockito.when(f.get()).then(i -> {
			Thread.sleep(1 * 1000);
			return "oups";
		}).thenReturn(null);
		saver.setController(controller);
		controller.proceed();
	}
}
