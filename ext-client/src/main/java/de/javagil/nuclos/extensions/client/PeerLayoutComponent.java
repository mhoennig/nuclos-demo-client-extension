package de.javagil.nuclos.extensions.client;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.nuclos.common.dal.vo.EntityObjectVO;

import de.javagil.nuclos.extensions.client.impl.NuclosEntityFieldLayoutComponentImpl;
import de.javagil.nuclos.extensions.client.impl.NuclosLayoutComponentContextImpl;


/**
 * This layout component is invisible, but it is to be added to layouts in which 
 * {@link BaseUniversalComponent} subclass instances need access to the surrounding entity instance.
 * 
 * <p>All the ugly implementation details are hidden in the intermediate superclass.</p>
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public class PeerLayoutComponent<PK> extends NuclosEntityFieldLayoutComponentImpl<PK, String> {

	private JComponent jComponent = new JPanel();

	/**
	 * Constructor as called by the {@link PeerLayoutComponentFactory}.
	 * 
	 * @param context interface to the Nuclos context and connection point for other UniversalComponents
	 */
	PeerLayoutComponent(NuclosLayoutComponentContext context) {
		super(context);
	}
	
	@Override
	public JComponent getComponent() {
		return jComponent;
	}

	@Override
	protected void attachEntityVO(EntityObjectVO<PK> entityVO) {
	}

	@Override
	protected void detachEntityVO() {
	}

}
