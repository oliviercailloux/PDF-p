package io.github.oliviercailloux.pdf_number_pages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class InputOutputComponent {

	private Optional<Path> inputPath;

	private Path outputPath;

	private Composite sourceComposite;

	Text sourceText;

	public InputOutputComponent() {
		inputPath = Optional.of(Paths.get(System.getProperty("user.home"), "in.pdf"));
		outputPath = Paths.get(System.getProperty("user.home"), "out.pdf");
	}

	public void askForInputFile() {
		final FileDialog fileDialog = new FileDialog(sourceComposite.getShell());
		fileDialog.setText("Select file to open");
		fileDialog.setFilterExtensions(new String[] { "*.pdf" });
		final String toOpen = fileDialog.open();
		if (toOpen != null) {
			inputPath = Optional.of(Paths.get(toOpen));
		}
	}

	public Optional<Path> getInputPath() {
		return inputPath;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public void init(Composite parent) {
		sourceComposite = new Composite(parent, SWT.NONE);
		sourceComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sourceComposite.setLayout(new GridLayout(2, false));

		final Button sourceButton = new Button(sourceComposite, SWT.NONE);
		sourceButton.setText("Source");
		sourceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		sourceText = new Text(sourceComposite, SWT.BORDER);
		setInputText();
		sourceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				askForInputFile();
				setInputText();
				App.getInstance().inputPathChanged();
			}
		});

		final Button destButton = new Button(sourceComposite, SWT.NONE);
		destButton.setText("Dest");
		destButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		final Text destText = new Text(sourceComposite, SWT.BORDER);
		destText.setText(outputPath.toString());
		destText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	public void setInputPath(Optional<Path> inputPath) {
		assert inputPath != null;
		this.inputPath = inputPath;
		setInputText();
	}

	public void setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
	}

	void setInputText() {
		final String text = inputPath.isPresent() ? inputPath.get().toString() : "None";
		sourceText.setText(text);
	}

}
