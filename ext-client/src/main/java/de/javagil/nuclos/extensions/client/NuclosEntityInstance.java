package de.javagil.nuclos.extensions.client;

import org.nuclos.api.UID;
import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.ui.layout.LayoutComponent.LayoutComponentType;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;

public interface NuclosEntityInstance<PK> extends LayoutComponentContext {

	@Override
	public UID getEntityUid();

	@Override
	public LayoutComponentType getType();

	/**
	 * @param entityUID UID of the entity class
	 * @return the meta data for the given entity class UID
	 */
	public EntityMeta<?> getEntityMeta();

	/**
	 * @param fieldName name of the entity field
	 * @return meta data of the given entity field
	 */
	public FieldMeta<?> getFieldMeta(String fieldName);
	
	/**
	 * 
	 * @return
	 */
	public String getOID();
	
	/**
	 * @param fieldName
	 * @return 
	 */
	public <FT> FT getFieldValue(String fieldName, Class<FT> fieldType);
	
	public boolean setFieldValue(String fieldName, Object value);
}
