package io.github.oliviercailloux.pdf_number_pages.gui.label_ranges_component;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.github.oliviercailloux.pdf_number_pages.gui.Controller;
import io.github.oliviercailloux.swt_tools.TextEditingSupport;

public class EditingSupportPrefix extends TextEditingSupport<Integer> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportPrefix.class);

	public EditingSupportPrefix(ColumnViewer viewer) {
		super(viewer, Integer.class);
	}

	@Override
	public String getValueTyped(Integer elementIndex) {
		assert elementIndex != null;
		LOGGER.debug("Getting value for: {}.", elementIndex);
		return Strings.nullToEmpty(Controller.getInstance().getLabelRangesByIndex().get(elementIndex).getPrefix());
	}

	@Override
	public void setValueTyped(Integer elementIndex, String value) {
		assert elementIndex != null;
		final Controller app = Controller.getInstance();
		app.getLabelRangesByIndex().setPrefix(elementIndex, value);
	}

}
