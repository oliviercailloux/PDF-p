package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;

import io.github.oliviercailloux.pdf_number_pages.model.IOutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineChanged;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;
import io.github.oliviercailloux.swt_tools.JFace;

public class OutlineComponent {

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(OutlineComponent.class);

	private Label label;

	@SuppressWarnings("unused")
	private final Color NOT_VALID_COLOR;

	private Composite outlineComposite;

	private Reader reader;

	private KeyAdapter tableKeyListener;

	private Tree tree;

	private Composite treeComposite;

	Clipboard clipboard;

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
		tableKeyListener = null;
		clipboard = null;
		NOT_VALID_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
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
		outlineComposite = new Composite(parent, SWT.NONE);
		final GridData layoutOCData = new GridData(SWT.FILL, SWT.FILL, true, true);
		outlineComposite.setLayoutData(layoutOCData);
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		outlineComposite.setLayout(layout);

		label = new Label(outlineComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setForeground(NOT_VALID_COLOR);

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

		clipboard = new Clipboard(outlineComposite.getDisplay());

		initViewer();
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		LOGGER.debug("Model changed, refreshing viewer.");
		viewer.refresh();
	}

	@Subscribe
	public void outlineChanged(OutlineChanged event) {
		/**
		 * This is irremediably flawed because of SWT, AFAIU. The tree could have
		 * duplicated (equal) elements, including thus duplicated tree paths. I see no
		 * way to systematically remove the right one. We must remove one anyway,
		 * because just refreshing the viewer sometimes leaves spurious elements at the
		 * bottom of the view, that are no more in the model.
		 */
		viewer.remove(event.getParent(), event.getChildNb());
		/**
		 * We need to refresh in order to make sure the viewer has correct data, because
		 * of the above remark.
		 */
		viewer.refresh();
		/**
		 * TODO no, the evoqued case fails anyway: no way to distinguish the right
		 * element even when deleting it (from the selection). The wrong element gets
		 * deleted even in the model. (Then it seems the refresh bug is triggered
		 * anyway, which I believe is linked to the presence of multiple equal
		 * elements.)
		 */
	}

	@Subscribe
	public void readEvent(@SuppressWarnings("unused") ReadEvent event) {
		setText();
	}

	public void setKeyListenerEnabled(boolean enabled) {
		/**
		 * We should NOT fiddle with key listeners while a key event is processed,
		 * otherwise, it makes SWT send the same (already processed) event again. Thus,
		 * this method should not be called on model change.
		 */
		LOGGER.debug("Setting key listener: {} (currently {}).", enabled ? "Enabled" : "Disabled",
				tree.getListeners(SWT.KeyDown).length);
		if (enabled) {
			/**
			 * We have to remove it just in case it was already registered, to avoid double
			 * registration.
			 */
			tree.removeKeyListener(tableKeyListener);
			tree.addKeyListener(tableKeyListener);
		} else {
			tree.removeKeyListener(tableKeyListener);
		}
		LOGGER.debug("Set key listener: {} (currently {}).", enabled ? "Enabled" : "Disabled",
				tree.getListeners(SWT.KeyDown));
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

		tableKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				LOGGER.debug("Received: {}.", e);
				LOGGER.debug("Outline: {}.", outline);
				if (e.character == SWT.DEL) {
					final IStructuredSelection sel = viewer.getStructuredSelection();
					for (Object o : sel.toList()) {
						LOGGER.debug("Deleting {}.", o);
						final OutlineNode toDelete = (OutlineNode) o;
						toDelete.getParent().get().remove(toDelete.getLocalOrder().get().intValue());
					}
					e.doit = false;
					clearError();
				} else if (e.character == '+') {
					LOGGER.debug("Pressed plus.");
					final IStructuredSelection sel = viewer.getStructuredSelection();
					for (Object o : sel.toList()) {
						LOGGER.debug("Proceeding on {}.", o);
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
					clearError();
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
					clearError();
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
					clearError();
				} else if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'v')) {
					LOGGER.debug("Paste key.");
					final TextTransfer transfer = TextTransfer.getInstance();
					final String contents = (String) clipboard.getContents(transfer);
					if (contents == null) {
						return;
					}
					final List<String> lines = Arrays.asList(contents.split("[\r\n]+"));
					if (lines.size() > 50) {
						showError("> 50 lines of text.");
						return;
					}
					final IStructuredSelection sel = viewer.getStructuredSelection();
					final List<OutlineNode> toSelect = Lists.newLinkedList();
					final List<Optional<OutlineNode>> selectedOutlines = Lists.newLinkedList();
					for (Object o : sel.toList()) {
						final OutlineNode selected = (OutlineNode) o;
						final Optional<OutlineNode> selectedOpt = Optional.of(selected);
						selectedOutlines.add(selectedOpt);
					}
					if (sel.isEmpty()) {
						selectedOutlines.add(Optional.empty());
					}
					for (Optional<OutlineNode> selectedOpt : selectedOutlines) {
						LOGGER.debug("Proceeding on {}.", selectedOpt);
						final int defaultPageNumber;
						if (selectedOpt.isPresent()) {
							defaultPageNumber = selectedOpt.get().getBookmark().get().getPhysicalPageNumber() + 1;
						} else {
							defaultPageNumber = 1;
						}
						Optional<OutlineNode> anchor = selectedOpt;
						for (String line : lines) {
							LOGGER.debug("Proceeding on {}.", line);
							final Optional<OutlineNode> newOutlineOpt = asOutlineNode(line, defaultPageNumber);
							if (!newOutlineOpt.isPresent()) {
								return;
							}
							final OutlineNode newOutline = newOutlineOpt.get();
							LOGGER.debug("Created {}.", newOutline);
							if (anchor.isPresent()) {
								newOutline.setAsNextSiblingOf(anchor.get());
							} else {
								outline.addAsLastChild(newOutline);
							}
							LOGGER.debug("Added {}.", newOutline);
							anchor = Optional.of(newOutline);
							toSelect.add(newOutline);
						}
					}
					viewer.setSelection(new StructuredSelection(toSelect.toArray()));
					clearError();
				} else {
					clearError();
				}
				LOGGER.debug("Outline: {}.", outline);
			}
		};

		setText();

	}

	private void setText() {
		LOGGER.debug("Setting text to: {}.", outline);
		final boolean enabled;
		if (reader.getLastReadEvent().isPresent()) {
			final ReadEvent readEvent = reader.getLastReadEvent().get();
			if (readEvent.outlineReadSucceeded()) {
				clearError();
				enabled = true;
			} else {
				final String error = !readEvent.getErrorMessage().isEmpty() ? readEvent.getErrorMessage()
						: readEvent.getErrorMessageOutline();
				showError(error);
				enabled = false;
			}
		} else {
			showError("Not read yet.");
			enabled = false;
		}
		setKeyListenerEnabled(enabled);
	}

	Optional<OutlineNode> asOutlineNode(String bookmarkStr, int defaultPageNumber) {
		final Optional<OutlineNode> newOutlineOpt;
		final int indexTab = bookmarkStr.lastIndexOf('\t');
		final String title;
		final int page;
		if (indexTab == -1) {
			title = bookmarkStr;
			page = defaultPageNumber;
			newOutlineOpt = Optional.of(OutlineNode.newOutline(new PdfBookmark(title, page)));
		} else if (indexTab == bookmarkStr.length() - 1) {
			showError("I do not understand '" + bookmarkStr + "'.");
			newOutlineOpt = Optional.empty();
		} else {
			final Integer parsedPage = Ints.tryParse(bookmarkStr.substring(indexTab + 1));
			if (parsedPage == null) {
				showError("I do not understand '" + bookmarkStr + "'.");
				newOutlineOpt = Optional.empty();
			} else {
				title = bookmarkStr.substring(0, indexTab);
				/** User input is probably 1-based, our index is 0-based */
				page = parsedPage.intValue() - 1;
				newOutlineOpt = Optional.of(OutlineNode.newOutline(new PdfBookmark(title, page)));
			}
		}
		return newOutlineOpt;
	}

	void clearError() {
		label.setVisible(false);
		((GridData) label.getLayoutData()).exclude = true;
		outlineComposite.layout();
	}

	void showError(String error) {
		assert error != null;
		assert !error.isEmpty();
		label.setVisible(true);
		((GridData) label.getLayoutData()).exclude = false;
		LOGGER.info("Setting outline error.");
		label.setText(error);
		outlineComposite.layout();
	}
}
