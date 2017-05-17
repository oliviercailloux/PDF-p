package io.github.oliviercailloux.pdf_number_pages.label_ranges_component;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.App;
import io.github.oliviercailloux.swt_tools.IntEditingSupport;

public class EditingSupportStart extends IntEditingSupport<Integer> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportStart.class);

	public EditingSupportStart(ColumnViewer viewer) {
		super(viewer, Integer.class);
	}

	@Override
	public int getIntValue(Integer elementIndex) {
		assert elementIndex != null;
		LOGGER.info("Getting value for: {}.", elementIndex);
		return App.getInstance().getLabelRangesByIndex().get(elementIndex).getStart();
	}

	@Override
	public void setIntValue(Integer elementIndex, int value) {
		assert elementIndex != null;
		final App app = App.getInstance();
		app.setStart(elementIndex, value);
	}

}
