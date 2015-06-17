package de.javagil.nuclos.extensions.client;

import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.ui.layout.LayoutComponentFactory;
import org.nuclos.client.common.MetaProvider;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import de.javagil.nuclos.extensions.client.impl.NuclosLayoutComponentContextImpl;

/**
 * Base class for LayoutComponentFactories.  LayoutComponents can be created directly, 
 * but only if created via factory, they can get access to the context. 
 *
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public abstract class NuclosEntityFieldLayoutComponentFactory<PK> implements LayoutComponentFactory<PK> {

	// needed in init of actual component
	@Autowired
	private MetaProvider mprov;
	
	// needed in init of actual component
	@Autowired
	private SpringLocaleDelegate locale;
	
	@Autowired(required=false)
	private EntityObjectDelegate eodlgt;
	
	@Autowired
    private ApplicationContext applicationContext;

	protected NuclosLayoutComponentContext adaptContext(LayoutComponentContext lcContext) {
		return new NuclosLayoutComponentContextImpl<PK>(lcContext, mprov, locale);
	}

}
