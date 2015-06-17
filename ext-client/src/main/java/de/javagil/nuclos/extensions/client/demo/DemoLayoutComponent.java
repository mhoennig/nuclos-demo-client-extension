package de.javagil.nuclos.extensions.client.demo;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.nuclos.common.dal.vo.EntityObjectVO;

import de.javagil.nuclos.extensions.client.NuclosLayoutComponentContext;
import de.javagil.nuclos.extensions.client.impl.NuclosEntityFieldLayoutComponentImpl;

/**
 * A demo class for NuclosEntityFieldLayoutComponent. 
 * 
 * <p>The problem with this kind of component is, that is has no connection to the actual entity instance field 
 * (just to a copy) and thus can't store anything.</p>
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public class DemoLayoutComponent<PK> extends NuclosEntityFieldLayoutComponentImpl<PK, String> {

	private JTextField fileNameEdit;
	
	/**
	 * Constructor, called by {@link DemoLayoutComponentFactory}.
	 * @param context
	 */
	public DemoLayoutComponent(NuclosLayoutComponentContext<PK> context) {
		super(context);
		getComponent().setToolTipText(DemoUniversalComponent.class.getSimpleName() + ": " + getFieldName());
	}
	
	@Override
	public JComponent getComponent() {
		if ( fileNameEdit == null ) {
			fileNameEdit = new JTextField();
			fileNameEdit.addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					updateValue();
				}
			});
		}
		return fileNameEdit;
	}
	
	@Override
	protected void attachEntityVO(EntityObjectVO<PK> entityVO) {
		String fileName = getFieldValue();
		fileNameEdit.setText(fileName);
		fileNameEdit.setEnabled(true);
	}

	@Override
	protected void detachEntityVO() {
		fileNameEdit.setText(null);
		fileNameEdit.setEnabled(false);
	}

	protected void updateValue() {
	}

}
