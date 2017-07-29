package io.github.oliviercailloux.pdf_number_pages.gui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import io.github.oliviercailloux.pdf_number_pages.model.ModelChanged;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.services.ReadEvent;
import io.github.oliviercailloux.pdf_number_pages.services.Reader;

public class OutlineComponent {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(OutlineComponent.class);

	private Outline outline;

	private Label outlineLabel;

	private Reader reader;

	public OutlineComponent() {
		outlineLabel = null;
		reader = null;
	}

	public Optional<Outline> getOutline() {
		return Optional.ofNullable(outline);
	}

	public Reader getReader() {
		return reader;
	}

	public void init(Composite parent) {
		final Composite draftComposite = new Composite(parent, SWT.NONE);
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		draftComposite.setLayoutData(layoutData);
		draftComposite.setLayout(new FillLayout());
		outlineLabel = new Label(draftComposite, SWT.NONE);
		setText();
	}

	@Subscribe
	public void modelChanged(@SuppressWarnings("unused") ModelChanged event) {
		setText();
	}

	@Subscribe
	public void readEvent(ReadEvent event) {
		outlineLabel.setEnabled(event.outlineReadSucceeded() && outline.isEmpty());
		setText();
	}

	public void setOutline(Outline outline) {
		this.outline = outline;
	}

	public void setReader(Reader reader) {
		this.reader = requireNonNull(reader);
	}

	private void setText() {
		LOGGER.info("Setting text.");
		if (reader.getLastReadEvent().isPresent()) {
			final ReadEvent readEvent = reader.getLastReadEvent().get();
			if (readEvent.outlineReadSucceeded()) {
				outlineLabel.setText(outline.toString());
			} else {
				outlineLabel.setText(readEvent.getErrorMessageOutline());
			}
		} else {
			outlineLabel.setText("Not read yet.");
		}
	}
}
