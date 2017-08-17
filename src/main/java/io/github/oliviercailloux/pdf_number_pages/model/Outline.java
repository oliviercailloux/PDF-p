package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * FIXME we listen to children, which post events. If children change parents?
 *
 * @author Olivier Cailloux
 *
 */
public class Outline implements IOutlineNode {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Outline.class);

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
		while(iterator.hasNext()) {
			final OutlineNode child = iterator.next();
			iterator.remove();
			child.removeParent();
			child.unregister(this);
		}
		post();
	}

	@Override
	public List<OutlineNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public Optional<IOutlineNode> getParent() {
		return Optional.empty();
	}

	@Subscribe
	public void modelChanged(ModelChanged event) {
		/** Let’s propagate events posted by my children. */
		eventBus.post(event);
	}

	@Override
	public void register(Object object) {
		eventBus.register(object);
	}

	@Override
	public boolean remove(OutlineNode child) {
		checkArgument(child.getParent().isPresent());
		final boolean removed = children.remove(child);
		/** Example: we remove element of index 0. */
		final int pos = child.getLocalOrder().get();
		/**
		 * Our iterator will first retrieve element of (new) index 0 (before remove: 1).
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
		post();
		return removed;
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
			eventBus.post(event);
		}
	}

	void setPostEnabled(boolean enabled) {
		this.postEnabled = enabled;
	}
}
