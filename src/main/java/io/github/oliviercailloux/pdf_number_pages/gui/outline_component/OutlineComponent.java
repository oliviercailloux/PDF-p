package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.IOutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.swt_tools.JFace;

public class OutlineComponent {

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(OutlineComponent.class);

	private Label label;

	private Composite outlineComposite;

	private Reader reader;

	private Tree tree;

	private Composite treeComposite;

	LabelRangesByIndex labelRangesByIndex;

	Outline outline;

	TreeViewer viewer;

	public OutlineComponent() {
		tree = null;
		reader = null;
		viewer = null;
		treeComposite = null;
		label = null;
		outlineComposite = null;
		labelRangesByIndex = null;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	public Outline getOutline() {
		return outline;
	}

	public Reader getReader() {
		return reader;
	}

	public void init(Composite parent) {
		initTree(parent);
		initViewer();
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		setText();
		LOGGER.debug("Model changed, refreshing viewer.");
		viewer.refresh();
	}

	@Subscribe
	public void readEvent(@SuppressWarnings("unused") ReadEvent event) {
		setText();
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public void setOutline(Outline outline) {
		this.outline = requireNonNull(outline);
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	private void initTree(Composite parent) {
		outlineComposite = new Composite(parent, SWT.NONE);
		final GridData layoutOCData = new GridData(SWT.FILL, SWT.FILL, true, true);
		outlineComposite.setLayoutData(layoutOCData);
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		outlineComposite.setLayout(layout);
		label = new Label(outlineComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		treeComposite = new Composite(outlineComposite, SWT.NONE);
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TreeColumnLayout treeLayout = new TreeColumnLayout(true);
		treeComposite.setLayout(treeLayout);
		tree = new Tree(treeComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		tree.setHeaderVisible(true);
		{
			final TreeColumn col = new TreeColumn(tree, SWT.NONE);
			col.setText("Text");
			treeLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
		{
			final TreeColumn col = new TreeColumn(tree, SWT.NONE);
			col.setText("Page Index");
			treeLayout.setColumnData(col, new ColumnWeightData(0, 100, true));
		}
		{
			final TreeColumn col = new TreeColumn(tree, SWT.NONE);
			col.setText("Page label");
			treeLayout.setColumnData(col, new ColumnWeightData(0, 100, true));
		}
		setText();
	}

	private void initViewer() {
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(new OutlineTreeContentProvider());
		{
			final EditingSupportTitle editingSupport = new EditingSupportTitle(viewer);
			JFace.addTextTreeViewerColumn(viewer, tree.getColumn(0), editingSupport);
		}
		{
			final EditingSupportPage editingSupport = new EditingSupportPage(viewer);
			JFace.addTextTreeViewerColumn(viewer, tree.getColumn(1), editingSupport);
		}
		{
			final EditingSupportPageLabel editingSupport = new EditingSupportPageLabel(viewer);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
			JFace.addTextTreeViewerColumn(viewer, tree.getColumn(2), editingSupport);
		}
		viewer.setInput(outline);
		viewer.refresh();

		final KeyAdapter tableKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				LOGGER.trace("Received: {}.", e);
				if (e.character == SWT.DEL) {
					final IStructuredSelection sel = viewer.getStructuredSelection();
					for (Object o : sel.toList()) {
						final OutlineNode toDelete = (OutlineNode) o;
						toDelete.getParent().get().remove(toDelete.getLocalOrder().get().intValue());
					}
					e.doit = false;
				} else if (e.character == '+') {
					LOGGER.debug("Pressed plus.");
					final IStructuredSelection sel = viewer.getStructuredSelection();
					for (Object o : sel.toList()) {
						final OutlineNode selected = (OutlineNode) o;
						final OutlineNode newOutline = OutlineNode.newOutline(
								new PdfBookmark("Title", selected.getBookmark().get().getPhysicalPageNumber() + 1));
						newOutline.setAsNextSiblingOf(selected);
					}
					if (sel.isEmpty()) {
						final OutlineNode newOutline = OutlineNode.newOutline(new PdfBookmark("Title", 0));
						outline.addAsLastChild(newOutline);
					}
					e.doit = false;
				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					final IStructuredSelection sel = viewer.getStructuredSelection();
					final List<OutlineNode> toExpand = Lists.newLinkedList();
					final Object[] expandedElements = viewer.getExpandedElements();
					for (Object element : expandedElements) {
						toExpand.add((OutlineNode) element);
					}
//					Collections.addAll(toExpand, (OutlineNode[]) expandedElements);
					for (Object o : sel.toList()) {
						final OutlineNode selected = (OutlineNode) o;
						final IOutlineNode parent = selected.getParent().get();
						final List<OutlineNode> siblings = parent.getChildren();
						final int selectedLocalOrder = selected.getLocalOrder().get();
						if (selectedLocalOrder != 0) {
							final OutlineNode newParent = siblings.get(selectedLocalOrder - 1);
							selected.changeParent(newParent);
							toExpand.add(newParent);
						}
					}
					viewer.setExpandedElements(toExpand.toArray());
					e.doit = false;
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					final IStructuredSelection sel = viewer.getStructuredSelection();
					final List<OutlineNode> toExpand = Lists.newLinkedList();
					final Object[] expandedElements = viewer.getExpandedElements();
					for (Object element : expandedElements) {
						toExpand.add((OutlineNode) element);
					}
					for (Object o : sel.toList()) {
						final OutlineNode selected = (OutlineNode) o;
						final IOutlineNode parent = selected.getParent().get();
						if (parent instanceof OutlineNode) {
							final int selectedOriginalLocalOrder = selected.getLocalOrder().get();
							final OutlineNode parentNode = (OutlineNode) parent;
							selected.setAsNextSiblingOf(parentNode);
							/**
							 * Let’s find the nodes which were next siblings of selected, at its original
							 * position. We want to set the selected one as their new parent.
							 */
							final ListIterator<OutlineNode> listIterator = parent.getChildren()
									.listIterator(selectedOriginalLocalOrder);
							if (listIterator.hasNext()) {
								/**
								 * If selected node will include new children, we make sure they will be
								 * visible.
								 */
								toExpand.add(selected);
							}
							while (listIterator.hasNext()) {
								final OutlineNode sibling = listIterator.next();
								sibling.changeParent(selected);
							}
						} else {
							checkState(parent instanceof Outline);
							LOGGER.debug("Impossible to move {} left.", selected);
						}
					}
					viewer.setExpandedElements(toExpand.toArray());
					e.doit = false;
				}
			}
		};
		tree.addKeyListener(tableKeyListener);
	}

	private void setText() {
		LOGGER.debug("Setting text to: {}.", outline);
		if (reader.getLastReadEvent().isPresent()) {
			final ReadEvent readEvent = reader.getLastReadEvent().get();
			if (readEvent.outlineReadSucceeded()) {
				label.setVisible(false);
				((GridData) label.getLayoutData()).exclude = true;
			} else {
				label.setVisible(true);
				((GridData) label.getLayoutData()).exclude = false;
				LOGGER.info("Setting outline error.");
				label.setText(!readEvent.getErrorMessage().isEmpty() ? readEvent.getErrorMessage()
						: readEvent.getErrorMessageOutline());
			}
		} else {
			label.setVisible(true);
			((GridData) label.getLayoutData()).exclude = false;
			label.setText("Not read yet.");
		}
	}
}
