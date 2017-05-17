package io.github.oliviercailloux.swt_tools;

import java.util.function.Function;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellEditorValidator;

public abstract class TypedEditingSupportConstantEditor<E, V> extends TypedEditingSupport<E, V> {

	private CellEditor editor;

	Function<Object, String> rawValueToErrorMessage;

	Function<V, String> valueToErrorMessage;

	public TypedEditingSupportConstantEditor(ColumnViewer viewer, Class<E> classOfElements) {
		super(viewer, classOfElements);
	}

	public TypedEditingSupportConstantEditor(ColumnViewer viewer, Class<E> classOfElements, Class<V> classOfValues) {
		super(viewer, classOfElements, classOfValues);
	}

	public CellEditor getCellEditor() {
		return editor;
	}

	@Override
	public CellEditor getCellEditorTyped(E element) {
		return editor;
	}

	public void setCellEditor(CellEditor editor) {
		this.editor = editor;
	}

	/**
	 * <p>
	 * Sets the input validator for this cell editor.
	 * </p>
	 * <p>
	 * The validator is given the value to be validated, and must return a
	 * string indicating whether the given value is valid; <code>null</code>
	 * means valid, and non-<code>null</code> means invalid, with the result
	 * being the error message to display to the end user.
	 * </p>
	 *
	 * @param rawValueToErrorMessage
	 *            the input validator, or <code>null</code> if none
	 */
	public void setFirstLevelValidator(Function<Object, String> rawValueToErrorMessage) {
		this.rawValueToErrorMessage = rawValueToErrorMessage;
		setValidator();
	}

	/**
	 * <p>
	 * Sets the input validator for this cell editor.
	 * </p>
	 * <p>
	 * The validator is given the value to be validated, and must return a
	 * string indicating whether the given value is valid; <code>null</code>
	 * means valid, and non-<code>null</code> means invalid, with the result
	 * being the error message to display to the end user.
	 * </p>
	 *
	 * @param valueToErrorMessage
	 *            the input validator, or <code>null</code> if none
	 */
	public void setValidator(Function<V, String> valueToErrorMessage) {
		this.valueToErrorMessage = valueToErrorMessage;
		setValidator();
	}

	private void setValidator() {
		if (rawValueToErrorMessage == null && valueToErrorMessage == null) {
			getCellEditor().setValidator(null);
			return;
		}
		getCellEditor().setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value) {
				final String firstLevelErrorMessage = rawValueToErrorMessage == null ? null
						: rawValueToErrorMessage.apply(value);
				if (firstLevelErrorMessage != null) {
					return firstLevelErrorMessage;
				}
				final V typedValue = getTypedValue(value);
				return valueToErrorMessage == null ? null : valueToErrorMessage.apply(typedValue);
			}
		});
	}

}
