package io.github.oliviercailloux.pdf_number_pages.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.github.oliviercailloux.pdf_number_pages.model.Outline;
import io.github.oliviercailloux.pdf_number_pages.model.OutlineNode;
import io.github.oliviercailloux.pdf_number_pages.model.PdfBookmark;

public class TestOutline {

	@Test
	public void test() {
		final Outline outline = new Outline();
		final OutlineNode n0 = OutlineNode.newOutline(new PdfBookmark());
		outline.addChild(0, n0);
		assertEquals(outline, n0.getParent().get());
		assertEquals(0, n0.getLocalOrder().get().intValue());

		final OutlineNode n01 = OutlineNode.newOutline(new PdfBookmark());
		n0.addAsLastChild(n01);
		assertEquals(n0, n01.getParent().get());
		n0.remove(n01);

		final OutlineNode n3 = OutlineNode.newOutline(new PdfBookmark());
		outline.addChild(1, n3);
		final OutlineNode n2 = OutlineNode.newOutline(new PdfBookmark());
		outline.addChild(1, n2);
		final OutlineNode n1 = OutlineNode.newOutline(new PdfBookmark());
		outline.addChild(1, n1);
		assertEquals(ImmutableList.of(n0, n1, n2, n3), outline.getChildren());
		assertEquals(0, n0.getLocalOrder().get().intValue());
		assertEquals(1, n1.getLocalOrder().get().intValue());
		assertEquals(2, n2.getLocalOrder().get().intValue());
		assertEquals(3, n3.getLocalOrder().get().intValue());

		outline.remove(n1);
		/** Now: n0, n2, n3. */
		assertEquals(ImmutableList.of(n0, n2, n3), outline.getChildren());
		assertEquals(0, n0.getLocalOrder().get().intValue());
		assertEquals(1, n2.getLocalOrder().get().intValue());
		assertEquals(2, n3.getLocalOrder().get().intValue());
		assertFalse(n1.getLocalOrder().isPresent());
		assertFalse(n1.getParent().isPresent());

		n2.setAsNextSiblingOf(n3);
		/** Now: n0, n3, n2. */
		assertEquals(ImmutableList.of(n0, n3, n2), outline.getChildren());
		assertEquals(0, n0.getLocalOrder().get().intValue());
		assertEquals(1, n3.getLocalOrder().get().intValue());
		assertEquals(2, n2.getLocalOrder().get().intValue());

		n3.changeParent(n0);
		/** Now: n0 -> n3, n2. */
		assertEquals(ImmutableList.of(n0, n2), outline.getChildren());
		assertEquals(0, n0.getLocalOrder().get().intValue());
		assertEquals(1, n2.getLocalOrder().get().intValue());
		assertEquals(ImmutableList.of(n3), n0.getChildren());
		assertEquals(0, n3.getLocalOrder().get().intValue());
		assertEquals(n0, n3.getParent().get());
	}

}
