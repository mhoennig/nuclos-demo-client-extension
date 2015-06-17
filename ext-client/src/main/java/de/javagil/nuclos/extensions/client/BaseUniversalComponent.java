package de.javagil.nuclos.extensions.client;

import javax.swing.JComponent;

import org.nuclos.common.collect.collectable.CollectableEntityField;

import de.javagil.nuclos.extensions.client.impl.BaseUniversalComponentImpl;

/**
 * A base class for the implementation of universal components.
 * 
 * <p>This abstract class demonstrates how the Nuclos API for UI components would be more powerful. 
 * As an intermediate superclass for application components, it defines the API but hides mostly all 
 * the ugly implementation stuff from application developers.
 * 
 * @author(C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public abstract class BaseUniversalComponent<PK> extends BaseUniversalComponentImpl<PK> {

	public BaseUniversalComponent(CollectableEntityField clctef, JComponent jComponent, boolean bSearchable) {
		super(clctef, jComponent, bSearchable);
	}
	
	/**
	 * Grants access to the surrounding entity instance in the status of load time.
	 * This instance is taken from the {@link PeerLayoutComponent}; if that was not yet initialized,
	 * or there is simply no loaded instance (e.g. freshly created or in search layout), 
	 * this method returns null.
	 * 
	 * @return the surrounding entity instance or null 
	 */
	@Override
	public NuclosEntityInstance<PK> getEntityInstance() {
		return super.getEntityInstance();
	}
	
	/**
	 * This method is called when the value changes. It can be overridden to validate the field.
	 * 
	 * @return True if the content is valid, False if not, null if validity can't be determined  
	 * (e.g. because the entity instance is yet unknown) 
	 */
	protected Boolean validate() {
		return true;
	}

}
