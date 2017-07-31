package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;

public class OutlineComponent {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(OutlineComponent.class);

	private Outline outline;

	private Tree outlineWidget;

	private Reader reader;

	public OutlineComponent() {
		outlineWidget = null;
		reader = null;
	}

	public Optional<Outline> getOutline() {
		return Optional.ofNullable(outline);
	}

	public Reader getReader() {
		return reader;
	}

	public void init(Composite parent) {
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		outlineWidget = new Tree(parent, SWT.MULTI);
		outlineWidget.setLayoutData(layoutData);
		outlineWidget.setHeaderVisible(true);
		{
			final TreeColumn col = new TreeColumn(outlineWidget, SWT.NONE);
			col.setText("Text");
			col.setWidth(100);
		}
		{
			final TreeColumn col = new TreeColumn(outlineWidget, SWT.NONE);
			col.setText("Index");
			col.setWidth(100);
		}
		setText();
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		setText();
	}

	@Subscribe
	public void readEvent(ReadEvent event) {
//		outlineWidget.setEnabled(event.outlineReadSucceeded() && outline.isEmpty());
		setText();
	}

	public void setOutline(Outline outline) {
		this.outline = outline;
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	private void populate(Widget parent, Iterable<OutlineNode> children) {
		assert parent instanceof Tree || parent instanceof TreeItem;
		for (OutlineNode outlineNode : children) {
			final TreeItem it1;
			if (parent instanceof TreeItem) {
				it1 = new TreeItem((TreeItem) parent, SWT.NONE);
			} else {
				it1 = new TreeItem((Tree) parent, SWT.NONE);
			}
			final PdfBookmark bookmark = outlineNode.getBookmark().get();
			it1.setText(0, bookmark.getTitle());
			it1.setText(1, "" + bookmark.getPhysicalPageNumber());
			populate(it1, outlineNode.getChildren());
		}
	}

	private void setText() {
		LOGGER.info("Setting text to: {}.", outline);
		outlineWidget.removeAll();
		if (reader.getLastReadEvent().isPresent()) {
			final ReadEvent readEvent = reader.getLastReadEvent().get();
			if (readEvent.outlineReadSucceeded()) {
				final Iterable<OutlineNode> children = outline.getChildren();
				populate(outlineWidget, children);
			} else {
				LOGGER.info("Setting outline error.");
				final TreeItem it1 = new TreeItem(outlineWidget, SWT.NONE);
				it1.setText(!readEvent.getErrorMessage().isEmpty() ? readEvent.getErrorMessage()
						: readEvent.getErrorMessageOutline());
			}
		} else {
			final TreeItem it1 = new TreeItem(outlineWidget, SWT.NONE);
			it1.setText("Not read yet.");
		}
	}
}
