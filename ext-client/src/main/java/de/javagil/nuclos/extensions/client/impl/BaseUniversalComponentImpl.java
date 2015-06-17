package de.javagil.nuclos.extensions.client.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.ref.WeakReference;
import java.util.EventListener;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.ui.CommonJTextField;
import org.nuclos.client.ui.FormatUtils;
import org.nuclos.client.ui.LayoutNavigationProcessor;
import org.nuclos.client.ui.collect.CommonController;
import org.nuclos.client.ui.collect.component.AbstractCollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableTextComponentHelper;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListenerSupport;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.gc.WeakCollectableComponentModelAdapter;
import org.nuclos.client.ui.labeled.LabeledTextComponent;
import org.nuclos.client.ui.message.MessageExchange;
import org.nuclos.client.ui.message.MessageExchange.MessageExchangeListener;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.UID;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.Pair;

import de.javagil.nuclos.extensions.client.BaseUniversalComponent;
import de.javagil.nuclos.extensions.client.NuclosEntityInstance;
import de.javagil.nuclos.extensions.util.Reflection;

/**
 * The implementation Base class for the implementation of universal components.
 * 
 * <p>This is so unbelievably ugly, better don't look at it if you just want to implement
 * a UniversalComponent bit justat the clean subclass {@link BaseUniversalComponent} in that case.
 * But if you want to understand what's currently not possible with official Nuclos APIs, this is your place.
 * This implementation will be replaced if Nuclos ever gets a sensible API for UI components.</p>
 * 
 * <p>The implementation is for the most part taken from Nuclos CollectableTextField, it's copied instead of inherited 
 * because only this way it's possible to exchange the Swing component (yes, ugly).</p>
 * 
 * @author(C)2015 <a href="mailto:michael@hoennig.de">Michael Hoennig</a>
 * @license http://de.wikipedia.org/wiki/GNU_Affero_General_Public_License
 * @see org.nuclos.client.ui.collect.component.CollectableTextField for original authorship of many parts
 */
public abstract class BaseUniversalComponentImpl<PK> extends AbstractCollectableComponent implements MessageExchangeListener {

	private static final Logger LOG = Logger.getLogger(BaseUniversalComponentImpl.class);
	protected CollectableEntityField clctef;
	private NuclosEntityInstance<PK> entityInstance;
	private boolean tryAquireEntityInstance = false;

	public BaseUniversalComponentImpl(CollectableEntityField clctef, JComponent jComponent, boolean bSearchable) {
		super(clctef, jComponent, bSearchable);
	    		  
		getJTextComponent().getDocument().addDocumentListener(newDocumentListenerForTextComponentWithComparisonOperator());

		this.getJTextComponent().addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
			}
			@Override
			public void focusGained(FocusEvent e) {
				tryAquireEntityInstance();
					
				if (selectAllOnGainFocus()) {
					final JTextComponent tcomp = getJTextComponent();
					if (tcomp.getHighlighter().getHighlights().length == 0) {
						tcomp.selectAll();
					}
				}
			}
		});
		
		this.clctef = clctef;
		FormatUtils.setupTextField(clctef, this.getJTextField(), bSearchable);
		MessageExchange.addListener(this);
		FormatUtils.setupTextField(clctef, this.getJTextField(), bSearchable);
		MessageExchange.addListener(this);
		  
		getJTextField().getDocument().addDocumentListener(new DocumentListener() {
		
			@Override
			public void removeUpdate(DocumentEvent e) {
				doValidate();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				doValidate();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				doValidate();
			}
		});
	}
	
	public NuclosEntityInstance<PK> getEntityInstance() {
		if ( entityInstance == null ) {
			NuclosLayoutComponentContextImpl.printParents(this, getJComponent());
			
			entityInstance = (NuclosEntityInstance<PK>) NuclosLayoutComponentContextImpl.findEntityInstance(getEntityField().getEntityUID(), getLayoutPane());
			tryAquireEntityInstance = entityInstance == null;
		}
		return entityInstance;
	}
	
	private Container getLayoutPane() {
		return getJComponent().getParent().getParent();
	}

	private void tryAquireEntityInstance() {
		if ( tryAquireEntityInstance ) {
			// maybe it's available by now, try to validate again
			getEntityInstance();
			doValidate();
		}
	}
	
	private void doValidate() {
		doValidateImpl(30);
	}
	
	// TODO: This mixes responsibilities and should be cleaned up:
	// It's one issue that validation against other field values 
	// is not possible as long as the peer was not found, 
	// but yet this 'peer not yet found' code should be separated.
	private void doValidateImpl(final int retry) {
		Boolean valid = validate();
		getJTextField().setEditable(valid != null);
		if ( valid != null ) {
			if ( valid ) {
				// indicates that the value is valid
				getJTextField().setBorder(null);
			} else {
				// indicates that the value is invalid
				getJTextField().setBorder(new LineBorder(Color.RED, 1));
			}
		} else {
			// indicates that validation was not yet possible due to missing reference
			getJTextField().setBorder(new LineBorder(Color.YELLOW, 1));
			doValidateRetryImpl(retry);
		}
	}

	private void doValidateRetryImpl(final int retry) {
		if ( retry > 0 ) {
			new Timer(500, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater( new Runnable() {
						@Override
						public void run() {
							doValidateImpl(retry-1);
						}
					});
				}
			}).start();
		}
	}

	protected abstract Boolean validate();

	// TODO it should not necessarily be a JTextField, and less so a CommonJTextField
    public CommonJTextField getJTextField() {
       return (CommonJTextField) this.getJTextComponent();
    }

    @Override
    public void setColumns(int iColumns) {
       this.getJTextField().setColumns(iColumns);
    }

    @Override
    public void setComparisonOperator(ComparisonOperator compop) {
    	// called from AbstractCollectableComponent
       super.setComparisonOperator(compop);

       if (compop.getOperandCount() < 2) {
         this.runLocked(new Runnable() {
            @Override
            public void run() {
            	try {
            		getJTextComponent().setText(null);
				}
				catch (Exception e) {
					LOG.error("CollectableTextField.setComparisionOperator: " + e, e);
				}            		
            }
         });
      }
    }

	private static class CollectableTextFieldCellRenderer implements TableCellRenderer {
			
		private final TableCellRenderer parentRenderer;
			
			// Don't use, this triggers a memory leak! (tp)
			// private final CommonJTextField ntf;

			private final int horizontalAlignment;
			
			private CollectableTextFieldCellRenderer(TableCellRenderer parentRenderer, CommonJTextField ntf) {
				this.parentRenderer = parentRenderer;
				this.horizontalAlignment = ntf.getHorizontalAlignment();
			}

			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus,
					int iRow, int iColumn) {
				final Component comp = parentRenderer.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow,
						iColumn);
				if (comp instanceof JLabel) {
					final JLabel lb = (JLabel) comp;
					lb.setHorizontalAlignment(horizontalAlignment);
				}
				return comp;
			}
		}

		@Override
		public TableCellRenderer getTableCellRenderer(boolean subform) {
			// called by diverse *TaskController, but yet no idea when - maybe if in subform? 
			final TableCellRenderer parentRenderer = super.getTableCellRenderer(subform);
			final CommonJTextField ntf = getJTextField();
			return new CollectableTextFieldCellRenderer(parentRenderer, ntf);
		}

	   @Override
	   public void receive(Object id, ObjectType type, MessageType msg) {
		   // MessageExchange handler, but when?
	      if(clctef != null && clctef.getCollectableEntity() != null) {
	         Pair<UID, UID> idPair = new Pair<UID, UID>(clctef.getCollectableEntity().getUID(), clctef.getUID());
	         if(idPair.equals(id))
	            if (type == MessageExchangeListener.ObjectType.TEXTFIELD)
	               if (msg == MessageExchangeListener.MessageType.REFRESH)
	            	   if (isSearchComponent())
	            		   FormatUtils.addAutoComplete(clctef, getJTextField(), FormatUtils.getAutoCompletePreferences(clctef));
	      }
	   }
	   

		protected LabeledTextComponent getLabeledTextComponent() {
			
			return (LabeledTextComponent) this.getJComponent();
		}

		protected JTextComponent getJTextComponent() {
			return this.getLabeledTextComponent().getJTextComponent();
		}

		@Override
		public boolean hasComparisonOperator() {
			// called from AbstractCollectableComponent
			return true;
		}

		@Override
		protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
			// called from AbstractCollectableComponent
			this.getJTextComponent().addMouseListener(popupmenulistener);
		}
		
		@Override
		public void setMnemonic(char cMnemonic) {
			// called from diverse classes, also superclass render, but when?
			super.setMnemonic(cMnemonic);
			getJTextComponent().setFocusAccelerator(cMnemonic);
		}

		@Override
		protected ComparisonOperator[] getSupportedComparisonOperators() {
			// called by AbstractCollectableComponent
			if(getEntityField().getJavaClass() == NuclosPassword.class)
				return new ComparisonOperator[] {
					ComparisonOperator.NONE,
					ComparisonOperator.EQUAL,
					ComparisonOperator.NOT_EQUAL };
			return super.getSupportedComparisonOperators();
		}

		// called indirectly from AbstractCollectableComponent.collectableFieldChangedInModel
		@Override
		protected void updateView(CollectableField clctfValue) {
			// might be necessary if field gets connected with another entity instance
			entityInstance = null;
			
			final CollectableFieldFormat clctfformat = CollectableFieldFormat.getInstance(this.getEntityField().getJavaClass());
			final String sText = clctfformat.format(this.getEntityField().getFormatOutput(), clctfValue.getValue());

			final JTextComponent textcomp = this.getJTextComponent();
			setText(sText);
			// ensure the start of the text is visible (instead of the end) when the text is too long
			// to be fully displayed:
			textcomp.setCaretPosition(0);

			this.adjustAppearance();
		}

		protected void setText(String sText) {
			// only called by myself
			this.getJTextComponent().setText(sText);
			
		}

		@Override
		public CollectableField getFieldFromView() throws CollectableFieldFormatException {
			// called from AbstractCollectableComponent and LayoutNavigationManager when this field was updated by user
			return CollectableTextComponentHelper.write(this.getJTextComponent().getText(), this.getEntityField());
		}

		@Override
		protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {
			// called from AbstractCollectableComponent on search
			return this.getSearchConditionFromViewImpl(this.getJTextComponent().getText());
		}

		/**
		 * Implementation of <code>CollectableComponentModelListener</code>.
		 * @param ev
		 */
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			// called indirectly from CollectableComponentModel.fireFieldChanged 
			if (this.isSearchComponent()) {
				// simply ignore this event
			}
			else { 
				GenericObjectCollectController gocc = determineGOCC(ev);
				use(gocc);
				super.collectableFieldChangedInModel(ev); // XXX potentielle context ID: ev.source.listenerSupport 
			}
		}
		
		@Override
		public void valueToBeChanged(DetailsComponentModelEvent ev) {
			GenericObjectCollectController gocc = determineGOCC(ev);
			use(gocc);
			super.valueToBeChanged(ev);
		}

		private GenericObjectCollectController determineGOCC(CollectableComponentModelEvent ev) {
			DetailsComponentModel source = Reflection.castIfPossible(ev.getSource(), DetailsComponentModel.class);
			if (source != null) {
				CollectableComponentModelListenerSupport listenerSupport = source.getListenerSupport();
				LinkedList<WeakCollectableComponentModelAdapter> lstListeners = 
						Reflection.getFieldValue(CollectableComponentModelListenerSupport.class, listenerSupport, "lstListeners");
				for ( WeakCollectableComponentModelAdapter adapter: lstListeners ) {
					Object wrapped = Reflection.getFieldValue(WeakCollectableComponentModelAdapter.class.getSuperclass(), adapter, "wrapped");
					WeakReference<EventListener> weakRef = Reflection.castIfPossible(wrapped, WeakReference.class);
					EventListener subContext = weakRef.get();
					if ( subContext != null ) {
						if ( subContext instanceof CollectableComponentModelListener ) {
							Object ccInner = Reflection.getFieldValueOrNull(subContext, "this$0");
							if (ccInner != null && ccInner instanceof CommonController) {
								Object cc = Reflection.getFieldValue(CommonController.class, ccInner, "cc");
								if ( cc != null && cc instanceof GenericObjectCollectController ) {
									return (GenericObjectCollectController)cc;
								}
							}
						}
					}
				}
			}
			return null;
		}

		protected void use(Object collEntity) {
		}

		/**
		 * Implementation of <code>CollectableComponentModelListener</code>.
		 * @param ev
		 */
		@Override
		public void searchConditionChangedInModel(final SearchComponentModelEvent ev) {
			
			// update the view:
			this.runLocked(new Runnable() {
				@Override
	            public void run() {
					// Note: CollectableTextComponent itself can only handle atomic search conditions.
					// If the following class cast should ever fail for a special text field, redefine searchConditionChangedInModel in your subclass:
					final AtomicCollectableSearchCondition atomiccond = (AtomicCollectableSearchCondition) ev.getSearchComponentModel().getSearchCondition();

					modelToView(atomiccond, BaseUniversalComponentImpl.this.getJTextComponent());
				}
			});
		}

		/**
		 * The "enabled" property of <code>CollectableTextComponent</code> is mapped to the
		 * "editable" property of <code>LabeledTextComponent</code> to ensure that copy (as in "copy&amp;paste")
		 * works on disabled text components.
		 * @param bEnabled
		 */
		@Override
		protected void setEnabledState(boolean bEnabled) {
			
			this.getLabeledTextComponent().setEditable(bEnabled);
		}

		@Override
		public void setInsertable(boolean bInsertable) {
			
		
			/** @todo check this */
//			this.getJTextComponent().setEditable(bInsertable);
		}

		protected boolean selectAllOnGainFocus() {
			// called by FocusEventListener (myself) when clicked in
			return true;
		}


		@Override
		public void bindLayoutNavigationSupportToProcessingComponent() {
			// called by LayoutNavigationManager - but no idea yet when
			if (getLabeledTextComponent().getJTextComponent() instanceof LayoutNavigationProcessor) {
				((LayoutNavigationProcessor)getLabeledTextComponent().getJTextComponent()).setLayoutNavigationCollectable(this);
			}
		}
}
