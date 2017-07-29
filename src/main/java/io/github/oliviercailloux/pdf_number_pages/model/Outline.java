package io.github.oliviercailloux.pdf_number_pages.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class Outline {
	private List<OutlineNode> children = Lists.newLinkedList();

	private EventBus eventBus = new EventBus();

	public boolean add(OutlineNode outline) {
		final boolean added = children.add(outline);
		if (added) {
			outline.register(this);
			eventBus.post(ModelChanged.newModelChangedAll());
		}
		return added;
	}

	public boolean addAll(Iterable<OutlineNode> outlines) {
		boolean addedSome = false;
		for (OutlineNode outline : outlines) {
			final boolean added = children.add(outline);
			if (added) {
				addedSome = true;
				outline.register(this);
			}
		}
		if (addedSome) {
			eventBus.post(ModelChanged.newModelChangedAll());
		}
		return addedSome;
	}

	public void clear() {
		if (children.isEmpty()) {
			return;
		}
		children.clear();
		eventBus.post(ModelChanged.newModelChangedAll());
	}

	public Iterable<OutlineNode> getChildren() {
		return Iterables.filter(children, (o) -> !o.isEmpty());
	}

	public boolean isEmpty() {
		return Iterables.isEmpty(getChildren());
	}

	@Subscribe
	public void modelChanged(ModelChanged event) {
		/** Let’s propagate events posted by my children. */
		eventBus.post(event);
	}

	public void register(Object object) {
		eventBus.register(object);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Children", children.toString()).toString();
	}
}
