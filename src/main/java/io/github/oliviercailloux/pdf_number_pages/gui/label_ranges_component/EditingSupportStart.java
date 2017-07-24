package io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component;

import static java.util.Objects.requireNonNull;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.swt_tools.IntEditingSupport;

public class EditingSupportStart extends IntEditingSupport<Integer> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportStart.class);

	private LabelRangesByIndex labelRangesByIndex;

	public EditingSupportStart(ColumnViewer viewer) {
		super(viewer, Integer.class);
		labelRangesByIndex = null;
	}

	@Override
	public int getIntValue(Integer elementIndex) {
		assert elementIndex != null;
		LOGGER.debug("Getting value for: {}.", elementIndex);
		return labelRangesByIndex.get(elementIndex).getStart();
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	@Override
	public void setIntValue(Integer elementIndex, int value) {
		assert elementIndex != null;
		labelRangesByIndex.setStart(elementIndex, value);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

}
