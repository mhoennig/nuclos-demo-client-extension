package de.javagil.nuclos.extensions.client;

/**
 * Used to notify changes between LayoutComponents and UniversalComponents 
 * which present the same entity field value.
 * 
 * <p>TODO: EXPERIMENTAL, not fully implemented yet.</p>
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public interface ValueChangeListener {

	void changed(Object realContext, String oid);

}
