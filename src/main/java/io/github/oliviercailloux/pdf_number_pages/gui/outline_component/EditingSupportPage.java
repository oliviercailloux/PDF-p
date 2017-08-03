package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.swt_tools.IntEditingSupport;

public class EditingSupportPage extends IntEditingSupport<Outline> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportPage.class);

	private LabelRangesByIndex labelRangesByIndex;

	private Outline outline;

	public EditingSupportPage(ColumnViewer viewer) {
		super(viewer, Outline.class);
		labelRangesByIndex = null;
		outline = null;
	}

	@Override
	public int getIntValue(Outline element) {
		assert element != null;
		LOGGER.debug("Getting value for: {}.", element);
		return element.get
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRangesByIndex;
	}

	@Override
	public void setIntValue(Outline element, int value) {
		assert elementIndex != null;
		labelRangesByIndex.setStart(elementIndex, value);
	}

	public void setLabelRangesByIndex(LabelRangesByIndex labelRangesByIndex) {
		this.labelRangesByIndex = requireNonNull(labelRangesByIndex);
	}

	public Outline getOutline() {
		return outline;
	}

	public void setOutline(Outline outline) {
		this.outline = requireNonNull(outline);
	}

}
