package io.github.oliviercailloux.pdf_number_pages.services.saver;

import java.nio.file.Path;

public class OutputPathChanged {

	private Path outputPath;

	public OutputPathChanged(Path outputPath) {
		this.outputPath = outputPath;
	}

	public Path getOutputPath() {
		return outputPath;
	}
}
