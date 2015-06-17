package de.javagil.nuclos.extensions.client.impl;

import java.awt.Container;

import org.nuclos.api.UID;

/**
 * Identifies an entity instance to be able to connect from a UniversalComponent to a LayoutComponent 
 * and thus get access to the surrounding entity instance.
 * 
 * <p>Currently it actually only identifies the entity class and the Swing container of the control.
 * Thus, the UniversalComponent and its LayoutComponent-peer have to be within the same subform.
 * It's a dirty hack, but I was not able to find better identifies with current Nuclos API.</p>
 * 
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public class EntityId<PK> {

	private UID entityUid;
	private Container container;

	public EntityId(UID entityUid, Container container) {
		this.entityUid = entityUid;
		this.container = container;
	}
	
	@Override
	public int hashCode() {
		return 31 + System.identityHashCode(container) + 13*entityUid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityId<?> other = (EntityId<?>)obj;
		return entityUid.equals(other.entityUid) && container == other.container;
	}

}
