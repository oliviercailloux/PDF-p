package io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component;

import java.util.EnumSet;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import io.github.oliviercailloux.pdf_number_pages.gui.Controller;
import io.github.oliviercailloux.pdf_number_pages.model.RangeStyle;
import io.github.oliviercailloux.swt_tools.ComboBoxEditingSupport;

public class EditingSupportStyle extends ComboBoxEditingSupport<Integer, RangeStyle> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportStyle.class);

	public EditingSupportStyle(ColumnViewer viewer) {
		super(viewer, Integer.class, RangeStyle.class);
		setItems(ImmutableList.copyOf(EnumSet.allOf(RangeStyle.class)));
	}

	@Override
	public RangeStyle getValueTyped(Integer elementIndex) {
		assert elementIndex != null;
		final PDPageLabelRange range = Controller.getInstance().getLabelRangesByIndex().get(elementIndex);
		final String style = range.getStyle();
		LOGGER.debug("Got style value for {}: {}.", elementIndex, style);
		return RangeStyle.fromPdfBoxStyle(style);
	}

	@Override
	public void setValueTyped(Integer elementIndex, RangeStyle value) {
		assert elementIndex != null;
		assert value != null;
		final Controller app = Controller.getInstance();
		app.getLabelRangesByIndex().setStyle(elementIndex, value);
	}

}
