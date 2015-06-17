package de.javagil.nuclos.extensions.client.demo;

import javax.swing.JComponent;

import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.client.ui.labeled.LabeledTextField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

import de.javagil.nuclos.extensions.WrappedException;
import de.javagil.nuclos.extensions.client.BaseUniversalComponent;
import de.javagil.nuclos.extensions.client.NuclosEntityInstance;

/**
 * An example component which uses the surrounding entity instance to validates against some other fields value.
 * Define the reference field name in the property PROPERTY_REFERENCEFIELD.
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public class DemoUniversalComponent<PK> extends BaseUniversalComponent<PK> {

	private static final String PROPERTY_REFERENCEFIELD = "Referenzfeld";

	public DemoUniversalComponent(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, createJComponent(clctef, bSearchable), bSearchable);
		try {
			setToolTipText(DemoUniversalComponent.class.getSimpleName() + ": " + getField().toDescription());
		} catch (CollectableFieldFormatException e) {
			new WrappedException(e);
		}
	}
	
	private static JComponent createJComponent(CollectableEntityField clctef, boolean bSearchable) {
		return new LabeledTextField(new LabeledComponentSupport(), 
	    		  clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable);
	}

	@Override
	protected Boolean validate() {
		NuclosEntityInstance<PK> ei = getEntityInstance();
		if ( ei == null ) {
			return null;
		}

		// this it just demonstrates how a validation against some other field could be implemented
		String currentValue = getJTextField().getText();
		String referenceFieldName = (String) getProperty(PROPERTY_REFERENCEFIELD);
		String referenceValue = ei.getFieldValue(referenceFieldName, String.class);
		return currentValue == null || referenceValue == null || currentValue.contains(referenceValue);
	}
}
