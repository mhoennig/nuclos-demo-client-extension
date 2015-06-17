package de.javagil.nuclos.extensions.client.impl;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.validation.constraints.NotNull;

import org.nuclos.api.Property;
import org.nuclos.api.context.LayoutComponentContext;
import org.nuclos.api.context.MultiContext;
import org.nuclos.api.context.SingleContext;
import org.nuclos.api.ui.layout.LayoutComponent;
import org.nuclos.api.ui.layout.LayoutComponentListener;
import org.nuclos.api.ui.layout.NewContext;
import org.nuclos.api.ui.layout.SearchContext;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.SystemUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonIOException;

import de.javagil.nuclos.extensions.WrappedException;
import de.javagil.nuclos.extensions.client.NuclosLayoutComponentContext;
import de.javagil.nuclos.extensions.client.PeerLayoutComponent;
import de.javagil.nuclos.extensions.client.SwingEntityFieldLayoutComponentContext;
import de.javagil.nuclos.extensions.client.ValueChangeListener;
import de.javagil.nuclos.extensions.util.Reflection;

/**
 * Contains all the ugly hacks to adapt to the Nuclos API to implement a LayoutComponent.
 * Currently only for the subclass {@link PeerLayoutComponent}.
 *
 * @author (C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 */
public abstract class NuclosEntityFieldLayoutComponentImpl<PK, FT> 
		implements LayoutComponent<PK>, LayoutComponentListener<PK>, SwingEntityFieldLayoutComponentContext<FT>, ValueChangeListener {


	private static final Class<?>[] oneBoolArg = {boolean.class};
	private static final Class<?>[] noArgs = {};
	public static final Class<?>[] nullArgs = null;

	@SuppressWarnings("serial")
	private Collection<LayoutComponentListener<PK>> lcListener = new ArrayList<LayoutComponentListener<PK>>() {{
			add(NuclosEntityFieldLayoutComponentImpl.this);
		}};
	private NuclosLayoutComponentContext<PK> ctx;
	private PK boId = null;

	public NuclosEntityFieldLayoutComponentImpl(NuclosLayoutComponentContext<PK> context) {
		ctx = context;
	}

	@Override
	public void addLayoutComponentListener(LayoutComponentListener<PK> listener) {
		this.lcListener.add(listener);
	}

	@Override
	public Collection<LayoutComponentListener<PK>> getLayoutComponentListeners() {
	    return this.lcListener;
	}

	@Override
	public void removeLayoutComponentListener(LayoutComponentListener<PK> listener) {
		this.lcListener.remove(listener);
	}

	@Override
	public JComponent getComponent(LayoutComponentType type) {
		if ( type != null && type != ctx.getType() ) {
			throw new IllegalStateException("Komponente wurde für " + ctx.getType() + " erzeugt, soll von Nuclos nun aber als " + type + " verwendet werden.");
		}
		return getComponent();
	}

	public abstract JComponent getComponent();

	@Override
	public EnumSet<LayoutComponentType> getSupportedTypes() {
	    return EnumSet.of(LayoutComponentType.DESIGN, LayoutComponentType.DETAIL);
	}

	@Override
	public void setBorder(Border arg0) {
	}

	/**
	 * Is called when this layout component is connected with an entity instance.
	 * 
	 * @param entityVO the entity instance
	 */
	protected abstract void attachEntityVO(@NotNull EntityObjectVO<PK> entityVO);

	private <CTX /*extends SingleContext|MultiContext|NewContext|SearchContext*/> void doAttachEntityVO(@NotNull EntityObjectVO<PK> entityVO, CTX context) {
		ctx.registerEntityInstance(boId, getFieldName(), entityVO, getLayoutPane()); 
		attachEntityVO(entityVO);
		determineRealContext(context);
		// ctx.broadcastValueChanged(ctx.getOID(entityVO));
	}

	// XXX: extremely ugly hack to identify the REAL context we are in.
	// Nuclos is lacking this functionality.
	private <CTX /*extends SingleContext|MultiContext|NewContext|SearchContext*/>  void determineRealContext(CTX context) {
		// context.this$0 ist der GenericObjectController
		GenericObjectCollectController realContext = Reflection.getFieldValue(context.getClass(), context, "this$0");
		Boolean isDetailsModeViewLoadingWithoutDependants = Reflection.callMethod(GenericObjectCollectController.class, realContext, "isDetailsModeViewLoadingWithoutDependants", noArgs);
		CollectableGenericObjectWithDependants clct = Reflection.callMethod( CollectController.class, realContext, "getCompleteSelectedCollectable", oneBoolArg, isDetailsModeViewLoadingWithoutDependants);
		ctx.attachRealContext(realContext, clct, getLayoutPane());
	}

	private Container getLayoutPane() {
		return getComponent().getParent().getParent().getParent();
	}

	private void doDetachEntityVO(@NotNull EntityObjectVO<PK> entityVO) {
		ctx.unregisterEntityInstance(boId, getFieldName(), entityVO, getLayoutPane());
		ctx.detachRealContext();
		detachEntityVO();
		// ctx.broadcastValueChanged(ctx.getOID(entityVO));
	}

	/**
	 * Is called when this layout component is connected from its entity instance.
	 */
	protected abstract void detachEntityVO();

	protected EntityObjectVO<PK> getEntityObjectVO() {
		return downcast(ctx.getEntityObjectVO(ctx.getEntityUid(), boId));		
	}

	@Override
	public void singleViewEntered(SingleContext<PK> context) {
		boId = context.getObjectId();
		determineRealContext(context);
		doAttachEntityVO(getEntityObjectVO(), context);
		getComponent(null).setEnabled(true);
	}

	@Override
	public void searchEntered(SearchContext context) {
		this.boId = null;
		getComponent(null).setEnabled(false);
	}

	@Override
	public void newEntered(NewContext context) {
		this.boId = null;
		getComponent(null).setEnabled(false);
	}

	@Override
	public void multiViewEntered(MultiContext<PK> context) {
		boId = null;
		getComponent(null).setEnabled(false);
		doDetachEntityVO(getEntityObjectVO());
	}

	@Override
	public void singleDeleteBefore(SingleContext<PK> context) {
	}

	@Override
	public void singleDeleteAfter(SingleContext<PK> context) {
		this.boId = null;
		getComponent(null).setEnabled(false);
	}

	@Override
	public void singleUpdateBefore(SingleContext<PK> context) {
	}

	@Override
	public void singleUpdateAfter(SingleContext<PK> context) {
		boId = null;
		getComponent(null).setEnabled(false);
		doDetachEntityVO(getEntityObjectVO());
	}

	@Override
	public void singleInsertBefore(SingleContext<PK> context) {
	}

	@Override
	public void singleInsertAfter(SingleContext<PK> context) {
		boId = context.getObjectId();
		doAttachEntityVO(getEntityObjectVO(), context);
		getComponent(null).setEnabled(true);
	}

	@Override
	public void multiDeleteBefore(MultiContext<PK> context) {
		boId = null;
		getComponent(null).setEnabled(false);
		doDetachEntityVO(getEntityObjectVO());
	}

	@Override
	public void multiDeleteAfter(MultiContext<PK> context) {
		boId = null;
		getComponent(null).setEnabled(false);
		doDetachEntityVO(getEntityObjectVO());
	}

	@Override
	public void setPreferences(org.nuclos.api.Preferences prefs) {
		// TODO: remove, just to explore Nuclos
		prefs.toString(); 
	}
	
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public String getMessage(String rid, String otext, Object... params) {
		return ctx.getLocaleDelegate().getMessage(rid, otext, params);
	}
	
	@Override
	public MouseListener createMouseListener(JPopupMenu newJPopupMenu) {
		return new DefaultJPopupMenuListener(newJPopupMenu);
	}

	@Override
	public void runCommand(final JComponent component, final Runnable runnable) {
		UIUtils.runCommandLater(component, new CommonRunnable() {

			@Override
			public void run() throws CommonBusinessException {
				// Durch dieses "hin- und her" bleibt die Swing Komponente Nuclos-frei. 
				try {
					runnable.run();
				} catch (WrappedException exc) {
					Class<? extends Throwable> causingExceptionClass = exc.getCause().getClass();
					if ( CommonBusinessException.class.isAssignableFrom(causingExceptionClass) ) {
						throw (CommonBusinessException) exc.getCause();
					} if ( IOException.class.isAssignableFrom(causingExceptionClass) ) {
						throw new CommonIOException((IOException)exc.getCause());
					}
					throw exc;
				}
				
			}
		});
	}

	@Override
	public void openFile(File file) {
		try {
			SystemUtils.open(file);
		} catch (IOException e) {
			throw new WrappedException(e);
		}
	}

	// TODO: move to context?
	@SuppressWarnings("unchecked")
	private <TT> TT downcast(Object instance) {
		return (TT)instance;
	}
	
	@Override
	public boolean isLayoutMode() {
		return ctx.getType() == LayoutComponentType.DESIGN;
	}


	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPreferredSize(Dimension preferredSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperty(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Property[] getComponentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getComponentPropertyLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LayoutComponentContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setFieldValue(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getProperty(String propName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FT getFieldValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void changed(Object realContext, String oid) {
		// TODO Auto-generated method stub
		
	}}
