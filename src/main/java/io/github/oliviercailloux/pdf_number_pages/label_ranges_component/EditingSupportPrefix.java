package io.github.oliviercailloux.pdf_number_pages.label_ranges_component;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.github.oliviercailloux.pdf_number_pages.App;
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
		LOGGER.info("Getting value for: {}.", elementIndex);
		return Strings.nullToEmpty(App.getInstance().getLabelRangesByIndex().get(elementIndex).getPrefix());
	}

	@Override
	public void setValueTyped(Integer elementIndex, String value) {
		assert elementIndex != null;
		final App app = App.getInstance();
		app.setPrefix(elementIndex, value);
	}

}
