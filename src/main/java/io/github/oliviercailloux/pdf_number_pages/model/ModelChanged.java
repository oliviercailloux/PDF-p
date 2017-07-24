package io.github.oliviercailloux.pdf_number_pages.model;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

public class ModelChanged {

	public static ModelChanged newModelChanged(ModelOperation op, int elementIndex) {
		final ModelChanged modelChanged = new ModelChanged();
		modelChanged.elementIndex = elementIndex;
		modelChanged.op = requireNonNull(op);
		return modelChanged;
	}

	public static ModelChanged newModelChangedAll() {
		return new ModelChanged();
	}

	private int elementIndex;

	private ModelOperation op;

	private ModelChanged() {
		elementIndex = -1;
		this.op = ModelOperation.ALL;
	}

	public int getElementIndex() {
		return elementIndex;
	}

	public ModelOperation getOp() {
		return op;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Op", op).add("Element index", elementIndex).toString();
	}
}
