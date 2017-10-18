package io.github.oliviercailloux.pdf_number_pages.model;

import java.util.List;
import java.util.Optional;

public interface IOutlineNode {

	public void addAsLastChild(OutlineNode child);

	public void addChild(int pos, OutlineNode outline);

	/**
	 * Not currently used. Though conceptually useful to distinguish equal nodes in
	 * a tree. Example: {(root, A1), (root, A2), (A1, B), (A2, B)}, with
	 * A1.equals(A2). With the ascendants plus the local order we get unique
	 * positions in the tree.
	 *
	 * @return not <code>null</code>, empty iff this node has no parent, modifiable,
	 *         does not write back.
	 */
	public List<IOutlineNode> getAscendants();

	public List<OutlineNode> getChildren();

	public Optional<IOutlineNode> getParent();

	public void register(Object object);

	/**
	 * Removal should be done by child number, not by child reference, as several
	 * equal children may exist.
	 *
	 * @param childNb
	 *            the local order of the child to remove.
	 */
	public void remove(int childNb);

	public void unregister(Object object);

	/**
	 * Two objects of this type are considered equal in the following cases.
	 * <ul>
	 * <li>If one of the objects is an {@link Outline}, they must have equal
	 * children and no data.</li>
	 * <li>If none of the objects is an {@link Outline}, they must both be
	 * {@link OutlineNode}s and must have equal data and equal children.</li>
	 * </ul>
	 * For two nodes to have equal children, it is required that the children be in
	 * the same order.
	 *
	 * @param o2
	 *            may be <code>null</code>.
	 * @return
	 */
	@Override
	boolean equals(Object o2);
}
