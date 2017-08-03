package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class OutlineNode {
	public static OutlineNode newEmptyOutline() {
		return new OutlineNode();
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
	 * May be <code>null</code> (to represent an empty tree).
	 */
	private PdfBookmark bookmark;

	private final List<OutlineNode> children = Lists.newLinkedList();

	private final EventBus eventBus = new EventBus();

	private OutlineNode() {
		bookmark = null;

	}

	public void addAsLastChild(OutlineNode childOutline) {
		requireNonNull(childOutline);
		childOutline.register(this);
		children.add(childOutline);
		/** TODO Post appropriate event. */
		eventBus.post(ModelChanged.newModelChangedAll());
	}

	public Optional<PdfBookmark> getBookmark() {
		/** Must have: bookmark == null ⇒ children is empty. */
		assert bookmark != null || Iterables.isEmpty(getChildren());
		return Optional.ofNullable(bookmark);
	}

	public Iterable<OutlineNode> getChildren() {
		return Iterables.filter(children, (o) -> !o.isEmpty());
	}

	public boolean isEmpty() {
		/** Must have: bookmark == null ⇒ children is empty. */
		assert bookmark != null || Iterables.isEmpty(getChildren());
		return bookmark == null;
	}

	@Subscribe
	public void modelChanged(ModelChanged event) {
		eventBus.post(event);
	}

	public void register(Object object) {
		eventBus.register(object);
	}

	public boolean setBookmark(PdfBookmark bookmark) {
		checkArgument(bookmark != null || Iterables.isEmpty(getChildren()));
		if (Objects.equal(this.bookmark, bookmark)) {
			return false;
		}
		this.bookmark = bookmark;
		eventBus.post(ModelChanged.newModelChangedAll());
		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Bookmark", bookmark).add("Children", children.toString())
				.toString();
	}

}
