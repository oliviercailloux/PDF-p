package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static java.util.Objects.requireNonNull;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.EditingSupportIndex;
import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.EditingSupportPrefix;
import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.EditingSupportStart;
import io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component.EditingSupportStyle;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;
import io.github.oliviercailloux.pdf_number_pages.model.RangeStyle;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.swt_tools.JFace;

public class OutlineComponent {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(OutlineComponent.class);

	private Outline outline;

	private Reader reader;

	private Tree tree;

	public OutlineComponent() {
		tree = null;
		reader = null;
	}

	public Outline getOutline() {
		return outline;
	}

	public Reader getReader() {
		return reader;
	}

	public void init(Composite parent) {
		initTree(parent);
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
		this.outline = requireNonNull(outline);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	private void initTree(Composite parent) {
		final TreeColumnLayout treeLayout = new TreeColumnLayout(true);
		final Composite treeComposite = new Composite(parent, SWT.NONE);
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		treeComposite.setLayoutData(layoutData);
		treeComposite.setLayout(treeLayout);
		tree = new Tree(treeComposite, SWT.MULTI);
		tree.setHeaderVisible(true);
		{
			final TreeColumn col = new TreeColumn(tree, SWT.NONE);
			col.setText("Text");
			col.setWidth(100);
			treeLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
		{
			final TreeColumn col = new TreeColumn(tree, SWT.NONE);
			col.setText("Index");
			treeLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
		setText();
	}

	private void initViewer() {
		final TreeViewer viewer = new TreeViewer(tree);
		final ArrayContentProvider provider = ArrayContentProvider.getInstance();
		viewer.setContentProvider(provider);
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer v, Object e1, Object e2) {
				final int i1 = (Integer) e1;
				final int i2 = (Integer) e2;
				return Integer.compare(i1, i2);
			}
		});
		{
			EditingSupportIndex editingSupport = new EditingSupportIndex(viewer);
			JFace.addTextTableViewerColumn(viewer, table.getColumn(COL_INDEX_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		{
			EditingSupportPrefix editingSupport = new EditingSupportPrefix(viewer);
			JFace.addTextTableViewerColumn(viewer, table.getColumn(COL_PREFIX_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		{
			EditingSupportStyle editingSupport = new EditingSupportStyle(viewer);
			JFace.addComboBoxTableViewerColumn(viewer, table.getColumn(COL_STYLE_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		{
			EditingSupportStart editingSupport = new EditingSupportStart(viewer);
			JFace.addTextTableViewerColumn(viewer, table.getColumn(COL_START_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		viewer.setInput(labelRangesByIndex.keySet());

		tableKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == DEL_KEY) {
					final IStructuredSelection sel = viewer.getStructuredSelection();
					for (Object o : sel.toList()) {
						final int elementIndex = (Integer) o;
						if (elementIndex == 0) {
							labelRangesByIndex.setPrefix(0, "");
							labelRangesByIndex.setStyle(0, RangeStyle.DECIMAL);
							labelRangesByIndex.setStart(0, 1);
						} else {
							labelRangesByIndex.removeExisting(elementIndex);
						}
					}
					e.doit = false;
				} else if (e.character == PLUS_KEY) {
					LOGGER.debug("Pressed plus.");
					if (!labelRangesByIndex.isEmpty()) {
						labelRangesByIndex.add();
						e.doit = false;
					}
				}
			}
		};
		table.addKeyListener(tableKeyListener);
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
		tree.removeAll();
		if (reader.getLastReadEvent().isPresent()) {
			final ReadEvent readEvent = reader.getLastReadEvent().get();
			if (readEvent.outlineReadSucceeded()) {
				final Iterable<OutlineNode> children = outline.getChildren();
				populate(tree, children);
			} else {
				LOGGER.info("Setting outline error.");
				final TreeItem it1 = new TreeItem(tree, SWT.NONE);
				it1.setText(!readEvent.getErrorMessage().isEmpty() ? readEvent.getErrorMessage()
						: readEvent.getErrorMessageOutline());
			}
		} else {
			final TreeItem it1 = new TreeItem(tree, SWT.NONE);
			it1.setText("Not read yet.");
		}
	}
}
