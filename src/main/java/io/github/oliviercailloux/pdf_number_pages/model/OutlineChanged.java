package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

/**
 *
 * TODO Fire the right event. Rename ModelChanged to LabelRangesChanged.
 * Currently we fire ModelChanged instead of OutlineChanged most of the time.
 *
 * @author Olivier Cailloux
 *
 */
public class OutlineChanged {

	public static OutlineChanged newOutlineChanged(OutlineOperation op, IOutlineNode parent, int childNb) {
		checkArgument(parent != null);
		checkArgument(childNb >= 0);
		/**
		 * Nb children used for comparision must be the number of children before
		 * deletion!
		 */
		checkArgument(childNb < parent.getChildren().size() + 1);
		final OutlineChanged changed = new OutlineChanged();
		changed.parent = parent;
		changed.childNb = childNb;
		changed.op = requireNonNull(op);
		return changed;
	}

	public static OutlineChanged newOutlineChangedAll() {
		return new OutlineChanged();
	}

	private int childNb;

	private OutlineOperation op;

	private IOutlineNode parent;

	private OutlineChanged() {
		parent = null;
		childNb = -1;
		this.op = OutlineOperation.ALL;
	}

	public int getChildNb() {
		return childNb;
	}

	public OutlineOperation getOp() {
		return op;
	}

	public IOutlineNode getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Op", op).add("Parent", parent).toString();
	}
}
