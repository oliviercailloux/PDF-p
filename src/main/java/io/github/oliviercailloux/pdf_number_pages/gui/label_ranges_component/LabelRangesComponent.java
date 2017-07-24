package io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component;

import static java.util.Objects.requireNonNull;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.RangeStyle;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.swt_tools.JFace;

public class LabelRangesComponent {
	private static final int COL_INDEX_IDX = 0;

	private static final int COL_PREFIX_IDX = 1;

	private static final int COL_START_IDX = 3;

	private static final int COL_STYLE_IDX = 2;

	private static final BiMap<Integer, String> COLUMN_NAMES = ImmutableBiMap.of(COL_INDEX_IDX, "Index", COL_PREFIX_IDX,
			"Prefix", COL_STYLE_IDX, "Style", COL_START_IDX, "Start");

	private static final char DEL_KEY = 0x7F;

	private static final char PLUS_KEY = 0x2B;

	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(LabelRangesComponent.class);

	private Table table;

	private Composite tableComposite;

	private KeyAdapter tableKeyListener;

	LabelRangesByIndex labelRangesByIndex;

	TableViewer viewer;

	public LabelRangesComponent() {
		tableKeyListener = null;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	/**
	 * Must have been initialized.
	 *
	 * @return not <code>null</code>.
	 */
	public TableViewer getViewer() {
		assert viewer != null;
		return viewer;
	}

	public void init(Composite parent) {
		initTable(parent);
		initViewer();
	}

	@Subscribe
	public void modelChangedEvent(ModelChanged event) {
		final int elementIndex = event.getElementIndex();
		LOGGER.debug("Model changed: {}.", event);
		switch (event.getOp()) {
		case ADD:
			viewer.add(elementIndex);
			break;
		case REMOVE:
			viewer.remove(elementIndex);
			break;
		case SET_PREFIX:
			viewer.update(elementIndex, new String[] { COLUMN_NAMES.get(COL_PREFIX_IDX) });
			break;
		case SET_START:
			viewer.update(elementIndex, new String[] { COLUMN_NAMES.get(COL_START_IDX) });
			break;
		case SET_STYLE:
			viewer.update(elementIndex, new String[] { COLUMN_NAMES.get(COL_STYLE_IDX) });
			break;
		case ALL:
			viewer.setInput(labelRangesByIndex.keySet());
			break;
		default:
			throw new IllegalStateException();
		}
	}

	@Subscribe
	public void readEvent(ReadEvent event) {
		setKeyListenerEnabled(event.succeeded());
	}

	public void setKeyListenerEnabled(boolean enabled) {
		if (enabled) {
			/**
			 * We have to remove it just in case it was already registered, to
			 * avoid double registration.
			 */
			table.removeKeyListener(tableKeyListener);
			table.addKeyListener(tableKeyListener);
		} else {
			table.removeKeyListener(tableKeyListener);
		}
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	private void initTable(Composite parent) {
		final TableColumnLayout tableLayout = new TableColumnLayout(true);
		tableComposite = new Composite(parent, SWT.NONE);
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		tableComposite.setLayoutData(layoutData);
		tableComposite.setLayout(tableLayout);
		table = new Table(tableComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		{
			final TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(COLUMN_NAMES.get(COL_INDEX_IDX));
			tableLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
		{
			final TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(COLUMN_NAMES.get(COL_PREFIX_IDX));
			tableLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
		{
			final TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(COLUMN_NAMES.get(COL_STYLE_IDX));
			tableLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
		{
			final TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(COLUMN_NAMES.get(COL_START_IDX));
			tableLayout.setColumnData(col, new ColumnWeightData(1, true));
		}
	}

	private void initViewer() {
		viewer = new TableViewer(table);
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
			JFace.getTextTableViewerColumn(viewer, table.getColumn(COL_INDEX_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		{
			EditingSupportPrefix editingSupport = new EditingSupportPrefix(viewer);
			JFace.getTextTableViewerColumn(viewer, table.getColumn(COL_PREFIX_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		{
			EditingSupportStyle editingSupport = new EditingSupportStyle(viewer);
			JFace.getComboBoxTableViewerColumn(viewer, table.getColumn(COL_STYLE_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}
		{
			EditingSupportStart editingSupport = new EditingSupportStart(viewer);
			JFace.getTextTableViewerColumn(viewer, table.getColumn(COL_START_IDX), editingSupport);
			editingSupport.setLabelRangesByIndex(labelRangesByIndex);
		}

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

}
