package io.github.oliviercailloux.pdf_number_pages.services.saver;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import io.github.oliviercailloux.pdf_number_pages.model.LabelRangesByIndex;
import io.github.oliviercailloux.pdf_number_pages.model.Outline;

public class SaveJob {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SaveJob.class);

	private Path inputPath;

	private LabelRangesByIndex labelRanges;

	/**
	 * May be <code>null</code>.
	 */
	private Outline outline;

	private Path outputPath;

	private boolean overwrite;

	public SaveJob(LabelRangesByIndex labelRanges, Outline outline, Path inputPath, Path outputPath,
			boolean overwrite) {
		this.labelRanges = LabelRangesByIndex.deepImmutableCopy(requireNonNull(labelRanges));
		this.outline = outline;
		checkArgument(!labelRanges.isEmpty());
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

	public Optional<Outline> getOutline() {
		return Optional.ofNullable(outline);
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
		helper.add("Input path", inputPath);
		helper.add("Output path", outputPath);
		helper.add("Overwrite", overwrite);
		helper.add("Label ranges", labelRanges);
		helper.add("Outline", outline);
		return helper.toString();
	}
}
