package io.github.oliviercailloux.swt_tools;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

public abstract class TypedEditingSupport<E, V> extends EditingSupport {

	private Function<Object, V> cellEditorValueToValue;

	private Class<E> classOfElements;

	private Function<V, ?> valueToCellEditorValue;

	public TypedEditingSupport(ColumnViewer viewer, Class<E> classOfElements) {
		super(viewer);
		assert classOfElements != null;
		this.classOfElements = classOfElements;
		setValueToCellEditorValue(Function.identity());
	}

	public TypedEditingSupport(ColumnViewer viewer, Class<E> classOfElements, Class<V> classOfValues) {
		this(viewer, classOfElements);
		setCellEditorValueToValue(o -> classOfValues.cast(o));
	}

	public boolean canEditTyped(@SuppressWarnings("unused") E element) {
		return true;
	}

	public abstract CellEditor getCellEditorTyped(E element);

	public E getTypedElement(Object element) {
		return classOfElements.cast(element);
	}

	public V getTypedValue(Object value) {
		return cellEditorValueToValue.apply(value);
	}

	/**
	 * <p>
	 * Get the value to set to the editor.
	 * </p>
	 * <p>
	 * This method is simply a better typed equivalent to
	 * {@link #getValue(Object)}.
	 * </p>
	 *
	 * @param element
	 *            the model element
	 * @return the value shown
	 */
	public abstract V getValueTyped(E element);

	public void setCellEditorValueToValue(Function<Object, V> cellEditorValueToValue) {
		Objects.requireNonNull(cellEditorValueToValue);
		this.cellEditorValueToValue = cellEditorValueToValue;
	}

	public void setValueToCellEditorValue(Function<V, ?> valueToCellEditorValue) {
		Objects.requireNonNull(valueToCellEditorValue);
		this.valueToCellEditorValue = valueToCellEditorValue;
	}

	/**
	 * <p>
	 * Sets the new value on the given element. Note that implementers need to
	 * ensure that <code>getViewer().update(element, null)</code> or similar
	 * methods are called, either directly or through some kind of listener
	 * mechanism on the implementer's model, to cause the new value to appear in
	 * the viewer.
	 * </p>
	 *
	 * <p>
	 * This method is simply a better typed equivalent to
	 * {@link #setValue(Object, Object)}.
	 * </p>
	 *
	 * @param element
	 *            the model element
	 * @param value
	 *            the new value
	 */
	public abstract void setValueTyped(E element, V value);

	@Override
	protected boolean canEdit(Object element) {
		return canEditTyped(getTypedElement(element));
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return getCellEditorTyped(getTypedElement(element));
	}

	@Override
	protected Object getValue(Object element) {
		return valueToCellEditorValue.apply(getValueTyped(getTypedElement(element)));
	}

	@Override
	protected void setValue(Object element, Object value) {
		setValueTyped(getTypedElement(element), getTypedValue(value));
	}
}
