package de.javagil.nuclos.extensions.client;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.nuclos.api.UID;
import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.ui.layout.LayoutComponent.LayoutComponentType;
import org.nuclos.client.common.MetaProvider;
import org.nuclos.client.dal.DalSupportForGO;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonPermissionException;

import de.javagil.nuclos.extensions.WrappedException;
import de.javagil.nuclos.extensions.client.impl.EntityId;
import de.javagil.nuclos.extensions.client.impl.NuclosEntityFieldLayoutComponentImpl;

@Deprecated
public class NuclosEntityFieldLayoutComponentContext<PK> implements LayoutComponentContext {

	// thanks to Frank Pavlic, see [1]
	private static final EntityObjectDelegate eodlgt = EntityObjectDelegate.getInstance();

	private static final Map<Object, Map<String, EntityObjectVO<?>>> instanceMaps = new HashMap<Object, Map<String, EntityObjectVO<?>>>();
	private static final Map<EntityId<?>, EntityObjectVO<?>> entityRegistry = new HashMap<EntityId<?>, EntityObjectVO<?>>();

	private LayoutComponentContext lcContext;
	private MetaProvider metaProvider;
	private SpringLocaleDelegate locale;

	private Object realContext;
	private CollectableGenericObjectWithDependants collectableGeneric;
	private EntityObjectVO<PK> realContextEntityObjectVO;

	public NuclosEntityFieldLayoutComponentContext(LayoutComponentContext lcContext, MetaProvider mprov, SpringLocaleDelegate locale) {
		this.lcContext = lcContext;
		this.metaProvider = mprov;
		this.locale = locale;
	}

	@Override
	public UID getEntityUid() {
		return lcContext.getEntityUid();
	}

	@Override
	public LayoutComponentType getType() {
		return lcContext.getType();
	}

	public EntityMeta<?> getEntityMeta(UID entityUID) {
		// thanks to Frank Pavlic, see [1]
		return metaProvider.getEntity((org.nuclos.common.UID) entityUID);
	}

	// TODO generic return type
	// thanks to Frank Pavlic, see [1] 
	public Object getFieldValue(EntityObjectVO<?> entityObjectVO, String fieldName) {
		if (entityObjectVO == null)
			return null;
		UID boUid = entityObjectVO.getDalEntity();
		
		FieldMeta<?> fm = getFieldMeta(boUid, fieldName);
		if ( fm != null ) {
			Object fid = entityObjectVO.getFieldId(fm.getUID());
			if ( fid != null ) {
				try {
					return eodlgt.get(fm.getForeignEntity(), fid);
				} catch (CommonPermissionException e) {
					throw new WrappedException(e);
				}
			}
			Object val = entityObjectVO.getFieldValue(fm.getUID());
			return val;
		}
		
		JOptionPane.showMessageDialog(null, "Kein Feld '" + fieldName + "' in der Entität. Bitte Eigenschaften der Layout Komponente überprüfen.");
		return null;
	}
	

	public String getOID(EntityObjectVO<?> entityObjectVO) {
		if (entityObjectVO == null) {
			return null;
		}
		return entityObjectVO.getDalEntity() + ":" + entityObjectVO.getPrimaryKey();
	}
	
	public String getOID(UID enityUID, Object pk) {
		if (enityUID == null) {
			return null;
		}
		return enityUID + ":" + pk;
	}
	
	
	
	public FieldMeta<?> getFieldMeta(UID boUid, String fieldName) {
		if ( boUid == null ) {
			return null;
		}
		EntityMeta<?> em = getEntityMeta(boUid);
		Collection<FieldMeta<?>> entfields = em.getFields();
		for (FieldMeta<?> fm : entfields) { 
			if (fm.getFieldName().equals(fieldName)) { 
				return fm;
			}
		}
		JOptionPane.showMessageDialog(null, "Kein Feld '" + fieldName + "' in der Entität. Bitte Eigenschaften der Layout Komponente überprüfen.");
		return null; 
	}
	
	public EntityMeta<?> getEntityMeta() {
		return getEntityMeta(getEntityUid());
	}

	public UID getFieldUid(UID boUid, String fieldName) {
		return getFieldMeta(boUid, fieldName).getUID();
	}

	public EntityObjectVO<?> getEntityObjectVO(UID entityUid, PK boId) {
		
		Map<String, EntityObjectVO<?>> instanceMap = instanceMaps.get(realContext);
		if ( instanceMap == null ) {
			instanceMap = new HashMap<String, EntityObjectVO<?>>();
			instanceMaps.put(realContext, instanceMap);
		}
		String oid = getOID(entityUid, boId);
		EntityObjectVO<?> instance = instanceMap.get(oid);
		if ( instance != null ) {
			return instance;
		}
		
		if ( realContext != null && collectableGeneric.getEntityUID().equals(entityUid) && collectableGeneric.getId().equals(boId) ) {
			instanceMap.put(oid, realContextEntityObjectVO);
			return realContextEntityObjectVO;
		}

		
		try {
			// thanks to Frank Pavlic, see [1] 
			EntityObjectVO<?> eo = eodlgt.get((org.nuclos.common.UID)entityUid, boId);
			instanceMap.put(oid, eo);
			return eo;
		} catch ( Exception exc ) {
			return null;
		}
	}

	public SpringLocaleDelegate getLocaleDelegate() {
		return locale;
	}

	// XXX ugly hack because the Nuclos context is just an instance of an inner class
	// and thus not the real context within wich entitiy instance have to be unique.
	public void setRealContext(Object realContext, CollectableGenericObjectWithDependants collectableGeneric) {
		this.realContext = realContext;
		this.collectableGeneric = collectableGeneric;
		realContextEntityObjectVO = (EntityObjectVO<PK>) DalSupportForGO.wrapGenericObjectVO(collectableGeneric.getGenericObjectCVO(), 
				(CollectableEOEntity) CollectableEOEntityClientProvider.getInstance().getCollectableEntity((org.nuclos.common.UID) getEntityUid())); 
	}

	public Object getRealContext() {
		return realContext;
	}
	
	public void registerEntityInstance(PK boId, String fieldName, EntityObjectVO<PK> entityVO, Container container) {
		EntityId<PK> entityId = new EntityId<PK>(getEntityUid(), container);
		entityRegistry.put(entityId, entityVO);		
	}
	
	public void unregisterEntityInstance(PK boId, String fieldName, EntityObjectVO<PK> entityVO, Container container) {
		EntityId<PK> entityId = new EntityId<PK>(getEntityUid(), container);
		entityRegistry.remove(entityId);
	}
	
	public static EntityObjectVO<?> findEntityInstance(UID entityUid, org.nuclos.common.UID fieldUid, Container container) {
		EntityMeta<?> entityMeta = MetaProvider.getInstance().getEntity((org.nuclos.common.UID)entityUid);
		FieldMeta<?> fieldMeta = entityMeta.getField(fieldUid);
		EntityId<?> entityId = new EntityId<Object>(entityUid, container);
		return entityRegistry.get(entityId);
	}
	
	@Override
	protected void finalize() throws Throwable {
		instanceMaps.remove(realContext);
		super.finalize();
	}
}

//[1] http://www.nuclos.de/de/forum/plugins/5401-universelle-komponente-selbst-implementieren.html?start=12#7379
