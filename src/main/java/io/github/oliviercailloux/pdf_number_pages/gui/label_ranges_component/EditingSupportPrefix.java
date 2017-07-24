package io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component;

import static java.util.Objects.requireNonNull;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.swt_tools.TextEditingSupport;

public class EditingSupportPrefix extends TextEditingSupport<Integer> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportPrefix.class);

	private LabelRangesByIndex labelRangesByIndex;

	public EditingSupportPrefix(ColumnViewer viewer) {
		super(viewer, Integer.class);
		labelRangesByIndex = null;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	@Override
	public String getValueTyped(Integer elementIndex) {
		assert elementIndex != null;
		LOGGER.debug("Getting value for: {}.", elementIndex);
		return Strings.nullToEmpty(labelRangesByIndex.get(elementIndex).getPrefix());
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	@Override
	public void setValueTyped(Integer elementIndex, String value) {
		assert elementIndex != null;
		labelRangesByIndex.setPrefix(elementIndex, value);
	}

}
