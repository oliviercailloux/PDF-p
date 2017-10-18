package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

/**
 * A node, either considered alone (root of a tree), in which case it has no
 * parent, or considered as part of a tree. In the last case, we also can query
 * the local order of the node. (This is important in order to distinguish this
 * node from another node with the same content (data and children) and same
 * parent.)
 *
 * @author Olivier Cailloux
 *
 */
public class OutlineNode implements IOutlineNode {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(OutlineNode.class);

	public static OutlineNode copyOf(OutlineNode source) {
		requireNonNull(source);
		final OutlineNode dest = new OutlineNode();
		dest.bookmark = source.bookmark;
		for (OutlineNode child : source.getChildren()) {
			LOGGER.debug("Adding to {}, {} as child.", dest, child);
			dest.addAsLastChild(copyOf(child));
		}
		return dest;
	}

	public static OutlineNode newOutline(PdfBookmark bookmark) {
		requireNonNull(bookmark);
		final OutlineNode outline = new OutlineNode();
		outline.setBookmark(bookmark);
		return outline;
	}

	public static OutlineNode newOutline(PdfBookmark bookmark, Iterable<OutlineNode> children) {
		requireNonNull(bookmark);
		requireNonNull(children);
		final OutlineNode outline = new OutlineNode();
		outline.setBookmark(bookmark);
		for (OutlineNode child : children) {
			outline.addAsLastChild(child);
		}
		return outline;
	}

	/**
	 * Not <code>null</code>.
	 */
	private PdfBookmark bookmark;

	private final Outline delegate;

	private int localOrder;

	private IOutlineNode parent;

	private OutlineNode() {
		bookmark = null;
		parent = null;
		delegate = new Outline() {
			@Override
			void addInternal(int pos, OutlineNode outline) {
				super.addInternal(pos, outline);
				/**
				 * This is really an ugly workaround for setting the parent to this object
				 * instead of the delegate.
				 */
				outline.setParent(OutlineNode.this, pos);
			}
		};
	}

	@Override
	public void addAsLastChild(OutlineNode childOutline) {
		delegate.addAsLastChild(childOutline);
	}

	@Override
	public void addChild(int pos, OutlineNode childOutline) {
		delegate.addChild(pos, childOutline);
	}

	/**
	 * Puts this as new last child of the given newparent. If the current parent is
	 * given, puts this as a last child, or do nothing if this object is already the
	 * last child.
	 *
	 * @param newParent
	 *            not <code>null</code>.
	 * @return <code>true</code> iff this object was not already the last child of
	 *         the given parent.
	 */
	public boolean changeParent(IOutlineNode newParent) {
		LOGGER.info("Changing parent from {} to {}.", parent, newParent);
		requireNonNull(newParent);
		if (newParent == this.parent) {
			final List<OutlineNode> siblings = this.parent.getChildren();
			if (siblings.get(siblings.size() - 1) == this) {
				return false;
			}
		}
		if (this.parent != null) {
			this.parent.remove(localOrder);
		}
		newParent.addAsLastChild(this);
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IOutlineNode)) {
			return false;
		}
		final IOutlineNode n2 = (IOutlineNode) obj;
		return Outline.areEqual(this, n2);
	}

	@Override
	public List<IOutlineNode> getAscendants() {
		final List<IOutlineNode> ascendants;
		if (parent == null) {
			ascendants = Lists.newLinkedList();
		} else {
			ascendants = parent.getAscendants();
		}
		ascendants.add(this);

		return ascendants;
	}

	public Optional<PdfBookmark> getBookmark() {
		return Optional.of(bookmark);
	}

	@Override
	public List<OutlineNode> getChildren() {
		return delegate.getChildren();
	}

	public Optional<Integer> getLocalOrder() {
		checkState((localOrder == -1) == (parent == null));
		return localOrder == -1 ? Optional.empty() : Optional.of(localOrder);
	}

	@Override
	public Optional<IOutlineNode> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public int hashCode() {
		return Outline.computeHashcode(this);
	}

	@Subscribe
	public void modelChanged(ModelChanged event) {
		delegate.post(event);
	}

	@Subscribe
	public void outlineChanged(@SuppressWarnings("unused") OutlineChanged event) {
		delegate.post(event);
	}

	@Override
	public void register(Object object) {
		delegate.register(object);
	}

	@Override
	public void remove(int childNb) {
		delegate.remove(childNb);
	}

	/**
	 * Moves this object just next to the given node. The newPreviousSibling and
	 * this object may have different parents. This object may have no parent.
	 *
	 * @param newPreviousSibling
	 *            not <code>null</code>.
	 * @return
	 */
	public boolean setAsNextSiblingOf(OutlineNode newPreviousSibling) {
		LOGGER.debug("Setting {} as next sibling of {}.", this, newPreviousSibling);
		final Optional<IOutlineNode> parentOpt = requireNonNull(newPreviousSibling).getParent();
		checkArgument(parentOpt.isPresent());
		final IOutlineNode newParent = parentOpt.get();
		checkState(newParent != null);
		final List<OutlineNode> newSiblings = newParent.getChildren();
		final int previousLocalOrder = newPreviousSibling.getLocalOrder().get();
		checkState(previousLocalOrder != -1);
		if (newParent == parent) {
			if (newSiblings.size() > previousLocalOrder + 1 && newSiblings.get(previousLocalOrder + 1) == this) {
				return false;
			}
		}
		if (parent != null) {
			parent.remove(localOrder);
		}
		/** The removal might have changed the previous local order. */
		newParent.addChild(newPreviousSibling.getLocalOrder().get() + 1, this);
		return true;
	}

	public boolean setBookmark(PdfBookmark bookmark) {
		checkArgument(bookmark != null || Iterables.isEmpty(getChildren()));
		if (Objects.equal(this.bookmark, bookmark)) {
			return false;
		}
		this.bookmark = bookmark;
		delegate.post(ModelChanged.newModelChangedAll());
		return true;
	}

	@Override
	public String toString() {
		/** Do not put the parent description, otherwise infinite recursion occurs. */
		return MoreObjects.toStringHelper(this).add("Bookmark", bookmark).add("Children", delegate.getChildren())
				.toString();
	}

	@Override
	public void unregister(Object object) {
		delegate.unregister(object);
	}

	/**
	 * For internal use only. Used to set parent pointer correctly after having
	 * changed children. Does not post events.
	 *
	 */
	void removeParent() {
		/**
		 * This check may fail because my parent may have as a child another object that
		 * equals me.
		 */
//		checkState(this.parent == null || !this.parent.getChildren().contains(this),
//				"I still am among my parent’s children!");
		this.parent = null;
		this.localOrder = -1;
	}

	void setLocalOrder(int localOrder) {
		checkState(parent.getChildren().get(localOrder) == this);
		this.localOrder = localOrder;
	}

	/**
	 * For internal use only. Used to set parent pointer correctly after having
	 * changed children. Does not post events.
	 *
	 * @param parent
	 *            not <code>null</code>.
	 */
	void setParent(IOutlineNode parent, int localOrder) {
		LOGGER.debug("Setting parent of {} to {}.", this, parent);
		requireNonNull(parent);
		checkArgument(localOrder >= 0);
		checkArgument(localOrder < parent.getChildren().size());
		checkState(parent.getChildren().get(localOrder) == this);
		this.parent = parent;
		this.localOrder = localOrder;
	}

}
