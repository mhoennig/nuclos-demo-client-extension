package de.javagil.nuclos.extensions.client;

import javax.swing.Icon;

import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.ui.Alignment;
import org.nuclos.api.ui.annotation.NucletComponent;
import org.nuclos.api.ui.layout.LayoutComponent;

import de.javagil.nuclos.extensions.client.NuclosEntityFieldLayoutComponentFactory;

// ACHTUNG: Muss in nuclos-extension-client-beans.xml aufgeführt sein!
@NucletComponent
public class PeerLayoutComponentFactory<PK> extends NuclosEntityFieldLayoutComponentFactory<PK> {

	@Override
	public Alignment getDefaulAlignment() {
		return null;
	}

	@Override
	public Object getDefaultPropertyValue(String propName) {
		return null;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return PeerLayoutComponent.class.getSimpleName();
	}

	@Override
	public LayoutComponent<PK>  newInstance(LayoutComponentContext lcContext) {
		return new PeerLayoutComponent<PK>(adaptContext(lcContext));
	}
}