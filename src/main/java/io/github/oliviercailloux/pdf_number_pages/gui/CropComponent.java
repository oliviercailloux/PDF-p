package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Floats;

import io.github.oliviercailloux.pdf_number_pages.model.BoundingBoxKeeper;
import io.github.oliviercailloux.pdf_number_pages.utils.BBPoint;
import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

/**
 * The UI uses the terms source and dest, but the code rather uses input and
 * output.
 *
 * @author Olivier Cailloux
 *
 */
public class CropComponent {
	@SuppressWarnings("unused")
	static final Logger LOGGER = LoggerFactory.getLogger(CropComponent.class);

	private BoundingBoxKeeper boundingBoxKeeper;

	private Composite composite;

	private Text left, bottom, right, top;

	@SuppressWarnings("unused")
	private final Color NOT_VALID_COLOR;

	public CropComponent() {
		left = null;
		NOT_VALID_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		boundingBoxKeeper = null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(8, false));
		final ModifyListener inputChecker = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				LOGGER.debug("Modifying {}.", e.data);
				checkInput();
			}

		};
		final VerifyListener listener = e -> {
			LOGGER.info("Verifying '{}'.", e.text);
			e.doit = e.text.matches("[-]?[0-9]*");
		};
		{
			final Label label = new Label(composite, SWT.NONE);
			label.setText("Left");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		left = new Text(composite, SWT.NONE);
		left.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		left.addModifyListener(inputChecker);
		left.addVerifyListener(listener);
		{
			final Label label = new Label(composite, SWT.NONE);
			label.setText("Bottom");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		bottom = new Text(composite, SWT.NONE);
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		bottom.addModifyListener(inputChecker);
		bottom.addVerifyListener(listener);
		{
			final Label label = new Label(composite, SWT.NONE);
			label.setText("Right");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		right = new Text(composite, SWT.NONE);
		right.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		right.addModifyListener(inputChecker);
		right.addVerifyListener(listener);
		{
			final Label label = new Label(composite, SWT.NONE);
			label.setText("Top");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		top = new Text(composite, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		top.addModifyListener(inputChecker);
		top.addVerifyListener(listener);

		checkInput();
		/** FIXME forbid strings equal to "-", … */
//		setFirstLevelValidator(v -> !v.matches("[-−]?[0-9]*") ? "Integer required." : null);
	}

	public void setBoundingBoxKeeper(BoundingBoxKeeper boundingBoxKeeper) {
		this.boundingBoxKeeper = requireNonNull(boundingBoxKeeper);
	}

	private Float asFloatAndFeedback(Text text) {
		final Float nb = Floats.tryParse(text.getText());
		/** Ugly result. */
		if (nb == null) {
			text.setBackground(NOT_VALID_COLOR);
		} else {
			text.setBackground(null);
		}
		return nb;
	}

	void checkInput() {
		final Float nbLeft = asFloatAndFeedback(left);
		final Float nbBottom = asFloatAndFeedback(bottom);
		final Float nbRight = asFloatAndFeedback(right);
		final Float nbTop = asFloatAndFeedback(top);
		if (nbLeft != null && nbBottom != null && nbRight != null && nbTop != null) {
			boundingBoxKeeper.setCropBox(new BBox(new BBPoint(nbLeft, nbBottom), new BBPoint(nbRight, nbTop)));
		} else {
			boundingBoxKeeper.removeCropBox();
		}
	}

}
