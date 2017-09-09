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
import io.github.oliviercailloux.pdf_number_pages.model.PdfPart;
import io.github.oliviercailloux.pdf_number_pages.utils.BBox;

public class SaveJob {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SaveJob.class);

	private Path inputPath;

	private Path outputPath;

	private boolean overwrite;

	private final PdfPart pdf;

	public SaveJob(LabelRangesByIndex labelRangesByIndex, Optional<Outline> outline, Optional<BBox> cropBox,
			Path inputPath, Path outputPath, boolean overwrite) {
		pdf = new PdfPart();
		pdf.setLabelRangesByIndex(LabelRangesByIndex.deepImmutableCopy(requireNonNull(labelRangesByIndex)));
		requireNonNull(outline);
		if (outline.isPresent()) {
			final Outline newOutline = new Outline();
			newOutline.addCopies(outline.get().getChildren());
			pdf.setOutline(newOutline);
		}
		checkArgument(!labelRangesByIndex.isEmpty());
		this.inputPath = requireNonNull(inputPath);
		this.outputPath = requireNonNull(outputPath);
		this.overwrite = overwrite;
		if (requireNonNull(cropBox).isPresent()) {
			pdf.setCropBox(cropBox.get());
		}
		LOGGER.debug("Set job: {}.", this);
	}

	public Optional<BBox> getCropBox() {
		return pdf.getCropBox();
	}

	public Path getInputPath() {
		return inputPath;
	}

	public LabelRangesByIndex getLabelRangesByIndex() {
		return pdf.getLabelRangesByIndex();
	}

	public Optional<Outline> getOutline() {
		return pdf.getOutline();
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public boolean getOverwrite() {
		return overwrite;
	}

	public PdfPart getPdf() {
		return pdf;
	}

	@Override
	public String toString() {
		final ToStringHelper helper = MoreObjects.toStringHelper(this);
		helper.add("Input path", inputPath);
		helper.add("Output path", outputPath);
		helper.add("Overwrite", overwrite);
		helper.add("Pdf", pdf);
		return helper.toString();
	}
}
