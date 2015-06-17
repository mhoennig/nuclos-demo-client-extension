package de.javagil.nuclos.extensions.client.impl;

import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.nuclos.api.UID;
import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.ui.layout.LayoutComponent.LayoutComponentType;
import org.nuclos.client.common.MetaProvider;
import org.nuclos.client.dal.DalSupportForGO;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.SpringLocaleDelegate;

import de.javagil.nuclos.extensions.client.NuclosEntityInstance;
import de.javagil.nuclos.extensions.client.NuclosEntityInstanceImpl;
import de.javagil.nuclos.extensions.client.NuclosLayoutComponentContext;
import de.javagil.nuclos.extensions.client.ValueChangeListener;

/**
 * Implementation of the entity instance load context in Nuclos layouts.
 * 
 * <p>Ugly code because there is not really an API in Nuclos for what we need.
 * If you just need this API, stick to the interface {$link LayoutComponentContext}.</p>
 *
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public class NuclosLayoutComponentContextImpl<PK> implements NuclosLayoutComponentContext<PK> {

	// thanks to Frank Pavlic, see [1]
	private static final EntityObjectDelegate eodlgt = EntityObjectDelegate.getInstance();

	private static final Map<Object, Map<String, EntityObjectVO<?>>> instanceMaps = new HashMap<Object, Map<String, EntityObjectVO<?>>>();
	private static final Map<EntityId<?>, NuclosEntityInstance<?>> entityRegistry = new HashMap<EntityId<?>, NuclosEntityInstance<?>>();

	private LayoutComponentContext lcContext;
	private MetaProvider metaProvider;
	private SpringLocaleDelegate locale;

	private Object realContext;
	private CollectableGenericObjectWithDependants collectableGeneric;
	private EntityObjectVO<PK> realContextEntityObjectVO;

	private Container container;

	// TODO: needs more intelligent data structure
	private final static List<ValueChangeListener> valueChangeListeners = new ArrayList<ValueChangeListener>();
	private static boolean inBroadcast = false;;

	public NuclosLayoutComponentContextImpl(LayoutComponentContext lcContext, MetaProvider mprov, SpringLocaleDelegate locale) {
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

	public EntityObjectVO<?> getEntityObjectVO(UID entityUid, PK boId) {
		debugging();
		
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

	// TODO needs more intelligent data structure, maybe Nuclos offers something?
	public synchronized void broadcastValueChanged(String oid) {
		if ( !inBroadcast ) {
			inBroadcast = true;
			for ( ValueChangeListener l: valueChangeListeners) {
				l.changed(realContext, oid);
			}
			inBroadcast = false;
		}
	}

	public void addValueChangeListener(NuclosEntityFieldLayoutComponentImpl<PK, ?> listener) {
		valueChangeListeners.add(listener);
	}

	public SpringLocaleDelegate getLocaleDelegate() {
		return locale;
	}

	// XXX ugly hack because the Nuclos context is just an instance of an inner class
	// and thus not the real context within which entity instance have to be unique.
	@Override
	public void attachRealContext(Object realContext, CollectableGenericObjectWithDependants collectableGeneric, Container container) {
		debugging();
				
		this.realContext = realContext;
		this.collectableGeneric = collectableGeneric;
		if (collectableGeneric != null) { 
			realContextEntityObjectVO = (EntityObjectVO<PK>) DalSupportForGO.wrapGenericObjectVO(collectableGeneric.getGenericObjectCVO(), 
				(CollectableEOEntity) CollectableEOEntityClientProvider.getInstance().getCollectableEntity((org.nuclos.common.UID) getEntityUid()));
		} else {
			realContextEntityObjectVO = null;
		}
		this.container = container;		
	}
	
	@Override
	public void detachRealContext() {
		realContext = null;
		collectableGeneric = null;
		container = null;
		realContextEntityObjectVO = null;
	}

	private static void debugging() {
		Thread thisThread = Thread.currentThread();
		thisThread.toString();
	}

	public Object getRealContext() {
		return realContext;
	}
	
	public void registerEntityInstance(Object boId, String fieldName, EntityObjectVO<?> entityVO, Container container) {
		EntityId<PK> entityId = new EntityId<PK>(getEntityUid(), container);
		NuclosEntityInstance<Object> entityInstance = new NuclosEntityInstanceImpl(this, (EntityObjectVO) entityVO);
		entityRegistry.put(entityId, entityInstance);		
	}
	
	public void unregisterEntityInstance(Object boId, String fieldName, EntityObjectVO<?> entityVO, Container container) {
		EntityId<PK> entityId = new EntityId<PK>(getEntityUid(), container);
		entityRegistry.remove(entityId);
	}
	

	public static NuclosEntityInstance<?> findEntityInstance(org.nuclos.common.UID entityUID, Container container) {
		debugging();
		EntityId<?> entityId = new EntityId<Object>(entityUID, container);
		return entityRegistry.get(entityId);
	}


	public MetaProvider getMetaProvider() {
		return metaProvider;
	}

	@Override
	protected void finalize() throws Throwable {
		instanceMaps.remove(realContext);
		super.finalize();
	}

	public static void printParents(Object comp, JComponent component) {
		String compString = comp.getClass() + "#" + System.identityHashCode(comp);
		for ( Container parent = component.getParent(); parent != null; parent = parent.getParent() ) {
			System.out.println(compString + ": " + parent.getClass() + "#" + System.identityHashCode(parent));
		}
	}
}

//[1] http://www.nuclos.de/de/forum/plugins/5401-universelle-komponente-selbst-implementieren.html?start=12#7379
