package io.github.oliviercailloux.pdf_number_pages.model;

import java.util.List;
import java.util.Optional;

public interface IOutlineNode {

	public void addAsLastChild(OutlineNode child);

	public void addChild(int pos, OutlineNode outline);

	public List<OutlineNode> getChildren();

	public Optional<IOutlineNode> getParent();

	public void register(Object object);

	public boolean remove(OutlineNode child);

	public void unregister(Object object);
}
