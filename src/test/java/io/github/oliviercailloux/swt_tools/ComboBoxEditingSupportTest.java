package io.github.oliviercailloux.swt_tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

public class ComboBoxEditingSupportTest {

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(ComboBoxEditingSupportTest.class);

	private Display display;

	private Shell shell;

	final HashBiMap<String, Double> strToD = HashBiMap.create();

	public void fireView() {
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	@Test
	public void test() {
		strToD.put("one", 1d);
		strToD.put("three", 3d);
		strToD.put("seven", 7d);
		strToD.put("fourteen", 14d);

		display = new Display();
		shell = new Shell(display);
		final TableViewer tableViewer = new TableViewer(shell);

		final ComboBoxEditingSupport<String, Double> ed = new ComboBoxEditingSupport<String, Double>(tableViewer,
				String.class, Double.class) {
			@Override
			public String asString(Double value) {
				LOGGER.debug("Value: {}.", value);
				return "The double: " + value;
			}

			@Override
			public Double getValueTyped(String element) {
				return strToD.get(element);
			}

			@Override
			public void setValueTyped(String element, Double value) {
				fail("Not used");
			}
		};
		ed.setItems(Lists.newArrayList(strToD.values()));

		ed.setValidator(d -> d > 10 ? "Too big!" : null);

		final ICellEditorValidator validator = ed.getComboBoxCellEditor().getValidator();
		assertNull(validator.isValid(1));
		assertNull(validator.isValid(2));
		assertNotNull(validator.isValid(3));
		ed.getComboBoxCellEditor().setValue(1);
		assertTrue(ed.getComboBoxCellEditor().isValueValid());
		assertEquals(1, ed.getComboBoxCellEditor().getValue());
		ed.getComboBoxCellEditor().setValue(3);
		assertFalse(ed.getComboBoxCellEditor().isValueValid());
		assertNull(ed.getComboBoxCellEditor().getValue());
	}

}
