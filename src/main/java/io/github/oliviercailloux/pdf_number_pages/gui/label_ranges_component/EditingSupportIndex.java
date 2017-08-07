package io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component;

import static java.util.Objects.requireNonNull;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.swt_tools.IntEditingSupport;

public class EditingSupportIndex extends IntEditingSupport<Integer> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportIndex.class);

	private LabelRangesByIndex labelRangesByIndex;

	public EditingSupportIndex(ColumnViewer viewer) {
		super(viewer, Integer.class);
		/** This prevents ints out of range, such as negative ones. */
		setIntegerValidator(intValue -> labelRangesByIndex.containsKey(intValue - 1) ? "Index must be unique." : null);
		labelRangesByIndex = null;
	}

	@Override
	public boolean canEditTyped(Integer element) {
		return element != 0;
	}

	@Override
	public int getIntValue(Integer elementIndex) {
		assert elementIndex != null;
		return elementIndex.intValue() + 1;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	@Override
	public void setIntValue(Integer elementIndex, int intValue) {
		assert elementIndex != null;
		if (elementIndex.intValue() == intValue - 1) {
			return;
		}
		labelRangesByIndex.move(elementIndex, intValue - 1);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

}
