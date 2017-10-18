package io.github.oliviercailloux.pdf_number_pages.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * TODO refactor this whole scheme. Basic idea is as follows. We need a root
 * that has no data but is a node. A node has an ordered list of nodes and
 * possibly data. A node knows its order number, this is useful for equality
 * test: two leaf nodes with same parent and same data are different. (We can
 * maybe drop this definition of equality, though.) If we drop it, a node may be
 * independent of its position in a tree, and then an extended node is a node
 * with a parent pointer and an order number. So that a node may be shared among
 * several trees: it would be cleaner (but we actually do not need this
 * functionality here). We also need a way to listen to changes in all children,
 * and insert a child at a specific position in the local order. (This is the
 * reason the existing tree library can’t be used.)
 *
 * We conceptually need three kinds of objects for a basic tree: node-with-data
 * (guarantee to contain data), node-without-data (guarantee to contain no
 * data), node (may have data). The last one is required to be able to apply
 * easily recursive algorithms on the tree. The node-without-data is probably
 * not required, can be replaced by a node that is not a node-with-data. But
 * what about equals? What about event listening? Furthermore, we need a
 * data-node-with-parent that encapsulates a node-with-data and that knows its
 * parent (a data-node-with-parent) and local order number. (Required for GUI
 * implementation.) A node-with-data may belong to several data-node-with-parent
 * s. Conceptually, a node-with-parent, that encapsulates a node, may exist, but
 * is probably never needed. It is not sufficient for two nodes to be equal to
 * have same children and data: this leads to a parent having possibly several
 * equal children, but that still need to be conceptually distinguished, which
 * e.g. makes viewer.remove(element) in SWT fail. (Or we could consider that it
 * is ok, as in a list.)
 *
 * @author Olivier Cailloux
 *
 */
public class Outline implements IOutlineNode {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Outline.class);

	public static boolean areEqual(IOutlineNode n1, IOutlineNode n2) {
		if (n1 instanceof Outline || n2 instanceof Outline) {
			final boolean n1hasData;
			final boolean n2hasData;
			if (n1 instanceof OutlineNode) {
				final OutlineNode on = (OutlineNode) n1;
				n1hasData = on.getBookmark().isPresent();
			} else {
				n1hasData = false;
			}
			if (n2 instanceof OutlineNode) {
				final OutlineNode on = (OutlineNode) n2;
				n2hasData = on.getBookmark().isPresent();
			} else {
				n2hasData = false;
			}
			return !n1hasData && !n2hasData && n1.getChildren().equals(n2.getChildren());
		}
		if (!(n1 instanceof OutlineNode) || !(n2 instanceof OutlineNode)) {
			throw new IllegalArgumentException(
					"If it’s not an Outline, and not an OutlineNode, then I don’t know what it is.");
		}
		final OutlineNode on1 = (OutlineNode) n1;
		final OutlineNode on2 = (OutlineNode) n2;
		return on1.getBookmark().equals(on2.getBookmark()) && on1.getChildren().equals(on2.getChildren());
	}

	public static int computeHashcode(IOutlineNode node) {
		/**
		 * Considering the contract of equals, the hashcode of an Outline must equal the
		 * hashcode of an OutlineNode with no data.
		 */
		int hash = Objects.hash(node.getChildren());
		if (node instanceof OutlineNode) {
			hash = hash + ((OutlineNode) node).getBookmark().hashCode();
		}
		return hash;
	}

	private List<OutlineNode> children = Lists.newLinkedList();

	private EventBus eventBus = new EventBus();

	private boolean postEnabled;

	public Outline() {
		postEnabled = true;
	}

	public void addAll(List<OutlineNode> outlines) {
		for (OutlineNode outline : outlines) {
			addInternal(outlines.size(), outline);
		}
		LOGGER.debug("Posting added.");
		post();
	}

	@Override
	public void addAsLastChild(OutlineNode outline) {
		int pos = children.size();
		addChild(pos, outline);
	}

	@Override
	public void addChild(int pos, OutlineNode outline) {
		addInternal(pos, outline);
		post();
	}

	public void addCopies(List<OutlineNode> sourceChildren) {
		for (OutlineNode child : sourceChildren) {
			addAsLastChild(OutlineNode.copyOf(child));
		}
	}

	public void clear() {
		if (children.isEmpty()) {
			return;
		}
		final Iterator<OutlineNode> iterator = children.iterator();
		while (iterator.hasNext()) {
			final OutlineNode child = iterator.next();
			iterator.remove();
			child.removeParent();
			child.unregister(this);
		}
		post();
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
		return areEqual(this, n2);
	}

	@Override
	public List<IOutlineNode> getAscendants() {
		return Lists.newLinkedList();
	}

	@Override
	public List<OutlineNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public Optional<IOutlineNode> getParent() {
		return Optional.empty();
	}

	@Override
	public int hashCode() {
		return computeHashcode(this);
	}

	@Subscribe
	public void modelChanged(ModelChanged event) {
		/** Let’s propagate events posted by my children. */
		post(event);
	}

	@Subscribe
	public void outlineChanged(OutlineChanged event) {
		post(event);
	}

	@Override
	public void register(Object object) {
		eventBus.register(object);
	}

	@Override
	public void remove(int childNb) {
		final OutlineNode child = children.remove(childNb);
		final Optional<IOutlineNode> parentOpt = child.getParent();
		assert parentOpt.isPresent();
		final IOutlineNode parent = parentOpt.get();
		/** Example: we remove element of index 0. */
		final int pos = child.getLocalOrder().get();
		assert pos == childNb;
		/**
		 * Our iterator will first retrieve element of (new) index 0.
		 */
		final ListIterator<OutlineNode> listIterator = children.listIterator(pos);
		/** i = 0. */
		int i = pos;
		while (listIterator.hasNext()) {
			/** Our first sibling is element of index 0. */
			final OutlineNode sibling = listIterator.next();
			/** It thinks it has local order 1. */
			assert sibling.getLocalOrder().get() == i + 1;
			/** Because of removal, it has local order 0. */
			sibling.setLocalOrder(i);
			++i;
		}
		child.removeParent();
		child.unregister(this);
		post(OutlineChanged.newOutlineChanged(OutlineOperation.REMOVE, parent, childNb));
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Children", children.toString()).toString();
	}

	@Override
	public void unregister(Object object) {
		eventBus.unregister(object);
	}

	void addInternal(int pos, OutlineNode outline) {
		children.add(pos, outline);
		final ListIterator<OutlineNode> listIterator = children.listIterator(pos + 1);
		int i = pos;
		while (listIterator.hasNext()) {
			++i;
			final OutlineNode child = listIterator.next();
			child.setLocalOrder(i);
		}
		outline.setParent(this, pos);
		outline.register(this);
	}

	void post() {
		post(ModelChanged.newModelChangedAll());
	}

	void post(ModelChanged event) {
		if (postEnabled) {
			LOGGER.debug("Posting {}.", event);
			eventBus.post(event);
		}
	}

	void post(OutlineChanged event) {
		if (postEnabled) {
			LOGGER.debug("Posting {}.", event);
			eventBus.post(event);
		}
	}

	void setPostEnabled(boolean enabled) {
		this.postEnabled = enabled;
	}
}
