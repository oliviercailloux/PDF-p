package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ModelChanged {

	private int elementIndex;

	private ModelOperation op;

	public ModelChanged(int elementIndex, ModelOperation op) {
		this.elementIndex = elementIndex;
		this.op = requireNonNull(op);
	}

	public ModelChanged(ModelOperation op) {
		checkArgument(op == ModelOperation.ALL);
		elementIndex = -1;
		this.op = op;
	}

	public int getElementIndex() {
		return elementIndex;
	}

	public ModelOperation getOp() {
		return op;
	}
}
