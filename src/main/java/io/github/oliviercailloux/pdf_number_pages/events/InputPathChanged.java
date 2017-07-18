package io.github.oliviercailloux.pdf_number_pages.events;

import java.nio.file.Path;

public class InputPathChanged {

	private Path inputPath;

	public InputPathChanged(Path inputPath) {
		this.inputPath = inputPath;
	}

	public Path getInputPath() {
		return inputPath;
	}
}
