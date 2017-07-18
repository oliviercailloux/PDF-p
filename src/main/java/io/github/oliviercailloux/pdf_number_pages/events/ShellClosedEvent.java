package io.github.oliviercailloux.pdf_number_pages.events;

import static java.util.Objects.requireNonNull;

import org.eclipse.swt.events.ShellEvent;

public class ShellClosedEvent {

	private ShellEvent e;

	public ShellClosedEvent(ShellEvent e) {
		this.e = requireNonNull(e);
	}

	/**
	 * @return a modifiable object.
	 */
	public ShellEvent getShellEvent() {
		return e;
	}

}
