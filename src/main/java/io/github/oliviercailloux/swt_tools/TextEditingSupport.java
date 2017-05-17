package io.github.oliviercailloux.swt_tools;

import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TextEditingSupport<E> extends TypedEditingSupportConstantEditor<E, String> {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(TextEditingSupport.class);

	/**
	 * @param viewer
	 *            must have a composite as underlying control.
	 */
	public TextEditingSupport(ColumnViewer viewer, Class<E> classOfElements) {
		super(viewer, classOfElements, String.class);
		setCellEditor(new TextCellEditor((Composite) viewer.getControl()));
	}

	public TextCellEditor getTextCellEditor() {
		return (TextCellEditor) getCellEditor();
	}
}