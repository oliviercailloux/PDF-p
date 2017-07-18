package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ForwardingNavigableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMap.Builder;
import com.google.common.eventbus.EventBus;

public class LabelRangesByIndex extends ForwardingNavigableMap<Integer, PDPageLabelRangeWithEquals>
		implements NavigableMap<Integer, PDPageLabelRangeWithEquals> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelRangesByIndex.class);

	public static LabelRangesByIndex deepImmutableCopy(LabelRangesByIndex source) {
		final Builder<Integer, PDPageLabelRangeWithEquals> builder = ImmutableSortedMap.naturalOrder();
		for (Entry<Integer, PDPageLabelRangeWithEquals> sourceEntry : source.entrySet()) {
			builder.put(sourceEntry.getKey(), new PDPageLabelRangeWithEquals(sourceEntry.getValue()));
		}
		return new LabelRangesByIndex(builder.build());
	}

	private final NavigableMap<Integer, PDPageLabelRangeWithEquals> delegate;

	final private EventBus eventBus = new EventBus(LabelRangesByIndex.class.getCanonicalName());

	public LabelRangesByIndex() {
		delegate = new TreeMap<>();
	}

	public LabelRangesByIndex(NavigableMap<Integer, PDPageLabelRangeWithEquals> delegate) {
		this.delegate = requireNonNull(delegate);
	}

	public void add() {
		final int newIndex;
		if (isEmpty()) {
			newIndex = 1;
		} else {
			final int lastKey = lastKey().intValue();
			newIndex = lastKey + 1;
		}
		final PDPageLabelRangeWithEquals range = new PDPageLabelRangeWithEquals();
		range.setStyle(PDPageLabelRange.STYLE_DECIMAL);
		putNew(newIndex, range);
	}

	public void addToDocument(PDDocument document) {
		final PDDocumentCatalog catalog = document.getDocumentCatalog();
		final PDPageLabels labels = toPDPageLabel(document);
		catalog.setPageLabels(labels);
	}

	public void move(int oldIndex, int newIndex) {
		final PDPageLabelRangeWithEquals old = remove(oldIndex);
		final PDPageLabelRangeWithEquals range = old;
		assert range != null;
		eventBus.post(new ModelChanged(oldIndex, ModelOperation.REMOVE));
		putNew(newIndex, range);
		LOGGER.debug("Removed {}, added {}.", oldIndex, newIndex);
		eventBus.post(new ModelChanged(newIndex, ModelOperation.ADD));
	}

	public void putNew(int index, PDPageLabelRangeWithEquals range) {
		final PDPageLabelRange previous = delegate.put(index, range);
		checkState(previous == null);
		LOGGER.debug("Putting new range {} at {}.", range, index);
		eventBus.post(new ModelChanged(index, ModelOperation.ADD));
	}

	public void register(Object listener) {
		eventBus.register(listener);
	}

	public void removeExisting(int index) {
		assert index != 0 : "Removal at first page not supported";
		final PDPageLabelRange old = delegate.remove(index);
		assert old != null;
		eventBus.post(new ModelChanged(index, ModelOperation.REMOVE));
	}

	public void setPrefix(int elementIndex, String prefix) {
		requireNonNull(prefix);
		final PDPageLabelRange element = get(elementIndex);
		checkArgument(element != null);
		element.setPrefix(Strings.emptyToNull(prefix));
		LOGGER.debug("Set prefix value for {}: {}.", elementIndex, prefix);
		eventBus.post(new ModelChanged(elementIndex, ModelOperation.SET_PREFIX));
	}

	public void setStart(int elementIndex, int start) {
		final PDPageLabelRange element = get(elementIndex);
		checkArgument(element != null);
		element.setStart(start);
		LOGGER.debug("Set start value for {}: {}.", elementIndex, start);
		eventBus.post(new ModelChanged(elementIndex, ModelOperation.SET_START));
	}

	public void setStyle(int elementIndex, RangeStyle style) {
		final PDPageLabelRange range = get(elementIndex);
		checkArgument(range != null);
		LOGGER.debug("Setting style value for {}: {}.", elementIndex, style);
		final String stylePdfBox = style.toPdfBoxStyle();
		range.setStyle(stylePdfBox);
		eventBus.post(new ModelChanged(elementIndex, ModelOperation.SET_STYLE));
	}

	public PDPageLabels toPDPageLabel(PDDocument document) {
		final PDPageLabels labels = new PDPageLabels(document);
		for (Entry<Integer, PDPageLabelRangeWithEquals> labelByIndex : entrySet()) {
			labels.setLabelItem(labelByIndex.getKey().intValue(), labelByIndex.getValue());
		}
		return labels;
	}

	@Override
	protected NavigableMap<Integer, PDPageLabelRangeWithEquals> delegate() {
		return delegate;
	}

}
