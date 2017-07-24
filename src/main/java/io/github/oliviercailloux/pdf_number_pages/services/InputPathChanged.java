package io.github.oliviercailloux.pdf_number_pages.services;

import java.nio.file.Path;

import com.google.common.base.MoreObjects;

public class InputPathChanged {

	private Path inputPath;

	public InputPathChanged(Path inputPath) {
		this.inputPath = inputPath;
	}

	public Path getInputPath() {
		return inputPath;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Input path", inputPath).toString();
	}
}
