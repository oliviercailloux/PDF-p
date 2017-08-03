package io.github.oliviercailloux.pdf_number_pages.gui.outline_component;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.google.common.collect.Iterables;

import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;

public class OutlineTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {
		final OutlineNode outlineNode = (OutlineNode) parentElement;
		return Iterables.toArray(outlineNode.getChildren(), OutlineNode.class);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		final OutlineNode outlineNode = (OutlineNode) inputElement;
		final Optional<PdfBookmark> bookmark = outlineNode.getBookmark();
		checkState(bookmark.isPresent());
		return new PdfBookmark[] { bookmark.get() };
	}

	@Override
	public Object getParent(Object element) {
		final OutlineNode outlineNode = (OutlineNode) element;
		TODO();
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		final OutlineNode outlineNode = (OutlineNode) element;
		TODO();
		return false;
	}

}
