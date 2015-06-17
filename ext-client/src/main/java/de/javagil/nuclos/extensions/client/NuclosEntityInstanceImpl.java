package de.javagil.nuclos.extensions.client;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.nuclos.api.UID;
import org.nuclos.api.ui.layout.LayoutComponent.LayoutComponentType;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityObjectVO;

import de.javagil.nuclos.extensions.client.impl.NuclosLayoutComponentContextImpl;

/**
*
* @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
* @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
*/
public class NuclosEntityInstanceImpl<PK> implements NuclosEntityInstance<PK> {

	private NuclosLayoutComponentContextImpl<PK> context;
	private CollectableGenericObjectWithDependants collectableGeneric;
	private EntityObjectVO<PK> entityVO;

	public NuclosEntityInstanceImpl(NuclosLayoutComponentContextImpl<PK> context, EntityObjectVO<PK> entityVO) {
		this.context = context;
		this.entityVO = entityVO;
	}

	@Override
	public UID getEntityUid() {
		return context.getEntityUid();
	}

	@Override
	public LayoutComponentType getType() {
		return context.getType();
	}

	@Override
	public EntityMeta<?> getEntityMeta() {
		// thanks to Frank Pavlic, see [1]
		return context.getMetaProvider().getEntity((org.nuclos.common.UID) context.getEntityUid());
	}

	@Override
	public FieldMeta<?> getFieldMeta(String fieldName) {
		EntityMeta<?> em = getEntityMeta();
		Collection<FieldMeta<?>> entfields = em.getFields();
		for (FieldMeta<?> fm : entfields) { 
			if (fm.getFieldName().equals(fieldName)) { 
				return fm;
			}
		}
		
		// TODO localize the error message
		JOptionPane.showMessageDialog(null, "Kein Feld '" + fieldName + "' in der Entität. Bitte Eigenschaften der Layout Komponente überprüfen.");
		return null;
	}

	private EntityObjectVO<?> getEntityObjectVO() {
		// TODO lazy loading
		return entityVO;
	}

	@Override
	public String getOID() {
		// TODO avoid using entitVO with lazy loading
		EntityObjectVO<?> entityVO = getEntityObjectVO();
		return entityVO.getDalEntity() + ":" + entityVO.getPrimaryKey();
	}
	
	@Override
	public <FT> FT getFieldValue(String fieldName, Class<FT> fieldType) {
		EntityObjectVO<?> entityObjectVO = getEntityObjectVO();
		
		// thanks to Frank Pavlic, see [1] 
		FieldMeta<?> fm = getFieldMeta(fieldName);
		if ( fm != null ) {	
			Object val = entityObjectVO.getFieldValue(fm.getUID());
			return (FT) val;
		}
		
		JOptionPane.showMessageDialog(null, "Kein Feld '" + fieldName + "' in der Entität. Bitte Eigenschaften der Layout Komponente überprüfen.");
		return null;
	}
	
	@Override
	public boolean setFieldValue(String fieldName, Object value) {
		EntityObjectVO<?> entityObjectVO = getEntityObjectVO();
		
		UID field = getFieldMeta(fieldName).getUID();
		if ( field != null ) {				
			entityObjectVO.setFieldValue((org.nuclos.common.UID)field, value);
			entityObjectVO.flagUpdate();
			collectableGeneric.getGenericObjectCVO().getAttribute((org.nuclos.common.UID) field).setValue(value); 
			collectableGeneric.setField((org.nuclos.common.UID)field, new CollectableValueField(value));
			context.broadcastValueChanged(getOID());
			return true;
		}

		// TODO Exception? ErrorMessage?
		return false; 
	}
	
}
