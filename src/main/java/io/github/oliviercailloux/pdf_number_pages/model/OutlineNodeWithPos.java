package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class OutlineNodeWithPos {
	static public OutlineNodeWithPos newNonRootOutlineNodeWithPos(OutlineNode node, List<Integer> pos) {
		checkNotNull(node);
		checkNotNull(pos);
		checkArgument(!pos.isEmpty());
		checkArgument(pos.get(pos.size() - 1) == node.getLocalOrder().get());
		final OutlineNodeWithPos o = new OutlineNodeWithPos();
		o.node = node;
		o.pos = pos;
		return o;
	}

	static public OutlineNodeWithPos newRootOutlineNodeWithPos(OutlineNode node) {
		checkNotNull(node);
		final OutlineNodeWithPos o = new OutlineNodeWithPos();
		o.node = node;
		o.pos = ImmutableList.of();
		return o;
	}

	private OutlineNode node;

	private List<Integer> pos;

	@Override
	public boolean equals(Object o2) {
		if (o2 == null) {
			return false;
		}
		if (!(o2 instanceof OutlineNodeWithPos)) {
			return false;
		}
		final OutlineNodeWithPos n2 = (OutlineNodeWithPos) o2;
		return Objects.equal(this.node, n2.node) && Objects.equal(this.pos, n2.pos);
	}

	public OutlineNode getNode() {
		return node;
	}

	/**
	 * @return not <code>null</code>, empty iff this node is root.
	 */
	public List<Integer> getPos() {
		return pos;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(node, pos);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(node).addValue(pos).toString();
	}
}
