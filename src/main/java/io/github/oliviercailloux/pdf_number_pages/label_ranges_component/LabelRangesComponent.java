package io.github.oliviercailloux.pdf_number_pages.label_ranges_component;

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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import io.github.oliviercailloux.pdf_number_pages.App;
import io.github.oliviercailloux.pdf_number_pages.JFace;
import io.github.oliviercailloux.pdf_number_pages.RangeStyle;

public class LabelRangesComponent {

	private static final int COL_INDEX_IDX = 0;

	private static final int COL_PREFIX_IDX = 1;

	private static final int COL_START_IDX = 3;

	private static final int COL_STYLE_IDX = 2;

	private static final BiMap<Integer, String> COLUMN_NAMES = ImmutableBiMap.of(COL_INDEX_IDX, "Index", COL_PREFIX_IDX,
			"Prefix", COL_STYLE_IDX, "Style", COL_START_IDX, "Start");

	private static final char DEL_KEY = 0x7F;

	private static final char PLUS_KEY = 0x2B;

	private Table table;

	private Composite tableComposite;

	private KeyAdapter tableKeyListener;

	TableViewer viewer;

	public LabelRangesComponent() {
		tableKeyListener = null;
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

	public void setKeyListenerEnabled(boolean enabled) {
		if (enabled) {
			table.addKeyListener(tableKeyListener);
		} else {
			table.removeKeyListener(tableKeyListener);
		}
	}

	public void updatePrefix(int elementIndex) {
		viewer.update(elementIndex, new String[] { COLUMN_NAMES.get(COL_PREFIX_IDX) });
	}

	public void updateStart(int elementIndex) {
		viewer.update(elementIndex, new String[] { COLUMN_NAMES.get(COL_START_IDX) });
	}

	public void updateStyle(int elementIndex) {
		viewer.update(elementIndex, new String[] { COLUMN_NAMES.get(COL_STYLE_IDX) });
	}

	private void initTable(Composite parent) {
		final TableColumnLayout tableLayout = new TableColumnLayout(true);
		tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayout(tableLayout);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
		JFace.getTextTableViewerColumn(viewer, table.getColumn(COL_INDEX_IDX), new EditingSupportIndex(viewer));
		JFace.getTextTableViewerColumn(viewer, table.getColumn(COL_PREFIX_IDX), new EditingSupportPrefix(viewer));
		JFace.getComboBoxTableViewerColumn(viewer, table.getColumn(COL_STYLE_IDX), new EditingSupportStyle(viewer));
		JFace.getTextTableViewerColumn(viewer, table.getColumn(COL_START_IDX), new EditingSupportStart(viewer));

		tableKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == DEL_KEY) {
					final IStructuredSelection sel = viewer.getStructuredSelection();
					for (Object o : sel.toList()) {
						final int elementIndex = (Integer) o;
						if (elementIndex == 0) {
							App.getInstance().setPrefix(0, "");
							App.getInstance().setStyle(0, RangeStyle.DECIMAL);
							App.getInstance().setStart(0, 1);
						} else {
							App.getInstance().remove(elementIndex);
						}
					}
					e.doit = false;
				} else {
					if (e.character == PLUS_KEY) {
						App.getInstance().add();
						e.doit = false;
					}
				}
			}
		};
	}

}
