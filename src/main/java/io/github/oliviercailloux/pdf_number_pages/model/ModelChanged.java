package io.github.oliviercailloux.pdf_number_pages.model;

import static java.util.Objects.requireNonNull;

public class ModelChanged {

	public static ModelChanged newModelChanged(int elementIndex, ModelOperation op) {
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
}
