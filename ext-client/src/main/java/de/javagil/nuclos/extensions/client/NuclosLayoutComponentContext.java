package de.javagil.nuclos.extensions.client;

import java.awt.Container;

import org.nuclos.api.UID;
import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;

public interface NuclosLayoutComponentContext<PK> extends LayoutComponentContext {

	public void registerEntityInstance(Object boId, String fieldName, EntityObjectVO<?> entityVO, Container container);
	
	public void unregisterEntityInstance(Object boId, String fieldName, EntityObjectVO<?> entityVO, Container container);
	
	public void attachRealContext(Object realContext, CollectableGenericObjectWithDependants collectableGeneric, Container container);

	public void detachRealContext();

	public Object getRealContext();

	public SpringLocaleDelegate getLocaleDelegate();
	
	@Deprecated
	public EntityObjectVO<?> getEntityObjectVO(UID entityUid, PK boId);

}
