package io.github.oliviercailloux.pdf_number_pages.model;

import java.util.List;
import java.util.Optional;

public interface IOutlineNode {

	public void addAsLastChild(OutlineNode child);

	public void addChild(int pos, OutlineNode outline);

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
