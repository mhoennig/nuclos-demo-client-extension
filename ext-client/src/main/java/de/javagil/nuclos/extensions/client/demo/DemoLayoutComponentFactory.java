package de.javagil.nuclos.extensions.client.demo;

import javax.swing.Icon;

import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.ui.Alignment;
import org.nuclos.api.ui.annotation.NucletComponent;
import org.nuclos.api.ui.layout.LayoutComponent;

import de.javagil.nuclos.extensions.client.NuclosEntityFieldLayoutComponentFactory;

/**
 * A factory for the DemoLayoutComponent.
 * 
 * ATTENTION: has to be configured in nuclos-extension-client-beans.xml
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
@NucletComponent
public class DemoLayoutComponentFactory<PK> extends NuclosEntityFieldLayoutComponentFactory<PK> {

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
		return DemoLayoutComponent.class.getSimpleName();
	}

	@Override
	public LayoutComponent<PK>  newInstance(LayoutComponentContext lcContext) {
		return new DemoLayoutComponent<PK>(adaptContext(lcContext));
	}
}