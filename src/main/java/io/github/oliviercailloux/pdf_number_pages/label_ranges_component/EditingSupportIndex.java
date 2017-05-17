package io.github.oliviercailloux.pdf_number_pages.label_ranges_component;

import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.pdf_number_pages.App;
import io.github.oliviercailloux.swt_tools.IntEditingSupport;

public class EditingSupportIndex extends IntEditingSupport<Integer> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EditingSupportIndex.class);

	public EditingSupportIndex(ColumnViewer viewer) {
		super(viewer, Integer.class);
		setIntegerValidator(intValue -> App.getInstance().getLabelRangesByIndex().containsKey(intValue - 1)
				? "Index must be unique." : null);
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

	@Override
	public void setIntValue(Integer elementIndex, int intValue) {
		assert elementIndex != null;
		if (elementIndex.intValue() == intValue - 1) {
			return;
		}
		final App app = App.getInstance();
		app.move(elementIndex, intValue - 1);
	}

}
