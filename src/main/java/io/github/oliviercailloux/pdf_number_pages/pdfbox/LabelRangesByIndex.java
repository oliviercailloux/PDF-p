package io.github.oliviercailloux.pdf_number_pages.pdfbox;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;

public class LabelRangesByIndex extends TreeMap<Integer, PDPageLabelRange> implements Map<Integer, PDPageLabelRange> {

	public void addToDocument(PDDocument document) {
		final PDDocumentCatalog catalog = document.getDocumentCatalog();
		final PDPageLabels labels = toPDPageLabel(document);
		catalog.setPageLabels(labels);
	}

	public PDPageLabels toPDPageLabel(PDDocument document) {
		final PDPageLabels labels = new PDPageLabels(document);
		for (Entry<Integer, PDPageLabelRange> labelByIndex : entrySet()) {
			labels.setLabelItem(labelByIndex.getKey().intValue(), labelByIndex.getValue());
		}
		return labels;
	}

}
