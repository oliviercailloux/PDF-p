package io.github.oliviercailloux.pdf_number_pages.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
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

	@Override
	public void clear() {
		super.clear();
		eventBus.post(ModelChanged.newModelChangedAll());
	}

	public void move(int oldIndex, int newIndex) {
		if (oldIndex == newIndex) {
			return;
		}
		LOGGER.info("Removing {}, adding {}.", oldIndex, newIndex);
		final PDPageLabelRangeWithEquals range = removeExisting(oldIndex);
		assert range != null;
		putNew(newIndex, range);
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends PDPageLabelRangeWithEquals> map) {
		super.putAll(map);
		LOGGER.debug("All put, firing.");
		eventBus.post(ModelChanged.newModelChangedAll());
	}

	public void putNew(int index, PDPageLabelRangeWithEquals range) {
		final PDPageLabelRange previous = delegate.put(index, range);
		checkState(previous == null);
		LOGGER.debug("Putting new range {} at {}.", range, index);
		eventBus.post(ModelChanged.newModelChanged(ModelOperation.ADD, index));
	}

	public void register(Object listener) {
		eventBus.register(listener);
	}

	public PDPageLabelRangeWithEquals removeExisting(int index) {
		assert index != 0 : "Removal at first page not supported";
		final PDPageLabelRangeWithEquals old = delegate.remove(index);
		assert old != null;
		eventBus.post(ModelChanged.newModelChanged(ModelOperation.REMOVE, index));
		return old;
	}

	public void setPrefix(int elementIndex, String prefix) {
		requireNonNull(prefix);
		final PDPageLabelRange range = get(elementIndex);
		checkArgument(range != null);
		final String newPrefix = Strings.emptyToNull(prefix);
		if (Objects.equal(range.getPrefix(), newPrefix)) {
			return;
		}
		range.setPrefix(newPrefix);
		LOGGER.debug("Set prefix value for {}: {}.", elementIndex, prefix);
		eventBus.post(ModelChanged.newModelChanged(ModelOperation.SET_PREFIX, elementIndex));
	}

	public void setStart(int elementIndex, int start) {
		final PDPageLabelRange range = get(elementIndex);
		checkArgument(range != null);
		if (range.getStart() == start) {
			return;
		}
		range.setStart(start);
		LOGGER.debug("Set start value for {}: {}.", elementIndex, start);
		eventBus.post(ModelChanged.newModelChanged(ModelOperation.SET_START, elementIndex));
	}

	public void setStyle(int elementIndex, RangeStyle style) {
		final PDPageLabelRange range = get(elementIndex);
		checkArgument(range != null);
		final String stylePdfBox = style.toPdfBoxStyle();
		if (Objects.equal(range.getStyle(), stylePdfBox)) {
			return;
		}
		LOGGER.debug("Setting style value for {}: {}.", elementIndex, style);
		range.setStyle(stylePdfBox);
		eventBus.post(ModelChanged.newModelChanged(ModelOperation.SET_STYLE, elementIndex));
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
