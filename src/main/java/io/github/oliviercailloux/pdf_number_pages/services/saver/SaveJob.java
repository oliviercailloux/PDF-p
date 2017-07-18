package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;

public class SaveJob {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SaveJob.class);

	private Path inputPath;

	private LabelRangesByIndex labelRanges;

	private Path outputPath;

	private boolean overwrite;

	public SaveJob(LabelRangesByIndex labelRanges, Path inputPath, Path outputPath, boolean overwrite) {
		this.labelRanges = LabelRangesByIndex.deepImmutableCopy(requireNonNull(labelRanges));
		this.inputPath = requireNonNull(inputPath);
		this.outputPath = requireNonNull(outputPath);
		this.overwrite = overwrite;
	}

	public Path getInputPath() {
		return inputPath;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return labelRanges;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public boolean getOverwrite() {
		return overwrite;
	}

	@Override
	public String toString() {
		final ToStringHelper helper = MoreObjects.toStringHelper(this);
		helper.add("input path", inputPath);
		helper.add("output path", outputPath);
		helper.add("overwrite", overwrite);
		helper.add("label ranges", labelRanges);
		return helper.toString();
	}
}
