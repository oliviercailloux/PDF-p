package io.github.oliviercailloux.swt_tools;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public abstract class ComboBoxEditingSupport<E, V> extends TypedEditingSupportConstantEditor<E, V> {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ComboBoxEditingSupport.class);

	private List<V> items;

	Class<V> classOfValues;

	/**
	 * @param viewer
	 *            must have a composite as underlying control.
	 */
	public ComboBoxEditingSupport(ColumnViewer viewer, Class<E> classOfElements, Class<V> classOfValues) {
		super(viewer, classOfElements, classOfValues);
		this.classOfValues = Objects.requireNonNull(classOfValues);
		final Function<Object, V> fromCellEditor = cellEditorValue -> {
			final int cellEditorIntValue = (int) cellEditorValue;
			assert cellEditorIntValue >= 0;
			assert cellEditorIntValue < items.size();
			return items.get(cellEditorIntValue);
		};
		setCellEditorValueToValue(fromCellEditor);
		final Function<V, ?> toCellEditor = v -> {
			final int index = items.indexOf(v);
			if (index == -1) {
				throw new IllegalStateException();
			}
			return index;
		};
		setValueToCellEditorValue(toCellEditor);
		setCellEditor(new ComboBoxViewerCellEditor((Composite) viewer.getControl()));
		/**
		 * TODO implement inputChanged on the array content provider to
		 * intercept input.
		 */
		getComboBoxCellEditor().setContentProvider(ArrayContentProvider.getInstance());
		getComboBoxCellEditor().setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object value) {
				final V typedValue = ComboBoxEditingSupport.this.classOfValues.cast(value);
				return asString(typedValue);
			}
		});
		final Function<Object, String> fromCellEditorVal = cellEditorValue -> {
			return cellEditorValue == null ? "The selection must be one of the provided choices." : null;
		};
		setFirstLevelValidator(fromCellEditorVal);
		/**
		 * Current problem: validator from above receives a RangeStyle, thus
		 * should not call getTypedValue which tries to convert from RangeStyle
		 * to Integer! Should remove the two-level validators, and simply
		 * override setValidator here so that it converts with class cast.
		 */
		setItems(null);
	}

	public String asString(V value) {
		return value.toString();
	}

	@Override
	public CellEditor getCellEditorTyped(E element) {
		return getCellEditor();
	}

	public ComboBoxViewerCellEditor getComboBoxCellEditor() {
		return (ComboBoxViewerCellEditor) getCellEditor();
	}

	/**
	 * Only returns the items if they have been set using this object
	 * constructor or {@link #setItems(List)}. If the items have been set (or
	 * modified) directly on the underlying widget, this object does not know
	 * them.
	 *
	 * @return the items, or <code>null</code> if not set.
	 */
	public List<V> getItems() {
		return items;
	}

	public void setItems(List<V> items) {
		final List<V> its = items == null ? ImmutableList.of() : items;
		this.items = its;
		getComboBoxCellEditor().setInput(items);
	}
}