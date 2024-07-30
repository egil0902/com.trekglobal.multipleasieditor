/******************************************************************************
 * Copyright (C) 2008 Low Heng Sin  All Rights Reserved.                      *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

package com.trekglobal.MultipleASIEditor.editor;



import static org.compiere.model.SystemIDs.COLUMN_M_PRODUCT_M_ATTRIBUTESETINSTANCE_ID;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.PAttributebox;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WEditorPopupMenu;
import org.adempiere.webui.event.ContextMenuEvent;
import org.adempiere.webui.event.ContextMenuListener;
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.window.WFieldRecordInfo;
import org.adempiere.webui.window.WPAttributeDialog;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_M_StorageOnHand;
import org.compiere.model.Lookup;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MDocType;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MMovementLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductionLine;
import org.compiere.model.MStorageOnHand;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

/**
 *
 * @author Low Heng Sin
 * 
 * @author Carlos Ruiz - add multi capture capabilities
 *
 */
public class WPAttributeMultipleEditor extends WEditor implements ContextMenuListener
{
	private static final String[] LISTENER_EVENTS = {Events.ON_CLICK, Events.ON_CHANGE, Events.ON_OK};

	private static final CLogger log = CLogger.getCLogger(WPAttributeMultipleEditor.class);

	private int m_WindowNo;

	private Lookup m_mPAttribute;

	private int m_C_BPartner_ID;

	private Object m_value;

	private GridTab m_GridTab;

	/**	No Instance Key					*/
	private static Integer		NO_INSTANCE = Integer.valueOf(0);

	public WPAttributeMultipleEditor(GridTab gridTab, GridField gridField)
	{
		super(new PAttributebox(), gridField);
		m_GridTab = gridTab;
		initComponents();
	}

	private void initComponents() {
		if (ThemeManager.isUseFontIconForImage())
			getComponent().getButton().setIconSclass("z-icon-PAttribute");
		else
			getComponent().setButtonImage(ThemeManager.getThemeResource("images/PAttribute16.png"));
		// getComponent().addEventListener(Events.ON_CLICK, this); // IDEMPIERE-426 - dup listener, already set at WEditor

		m_WindowNo = gridField.getWindowNo();
		m_mPAttribute = gridField.getLookup();
		m_C_BPartner_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "C_BPartner_ID");

		//	Popup
		popupMenu = new WEditorPopupMenu(true, false, false);
		addChangeLogMenu(popupMenu);
		
	}

	@Override
	public PAttributebox getComponent()
	{
		return (PAttributebox) component;
	}

	@Override
	public void setValue(Object value)
	{
		if (value == null || NO_INSTANCE.equals(value))
		{
			getComponent().setText("");
			m_value = value;
			return;
		}

		//	The same
		if (value.equals(m_value))
			return;
		
		ValueChangeEvent evt = new ValueChangeEvent(this, this.getColumnName(), getValue(), value);
		// -> ADTabpanel - valuechange
		fireValueChange(evt);
		
		//	new value
		if (log.isLoggable(Level.FINE)) log.fine("Value=" + value);
		m_value = value;
		getComponent().setText(m_mPAttribute.getDisplay(value));	//	loads value
	}

	@Override
	public Object getValue()
	{
		return m_value;
	}

	@Override
	public String getDisplay()
	{
		return getComponent().getText();
	}

	public void onEvent(Event event)
	{
		if (Events.ON_CHANGE.equals(event.getName()) || Events.ON_OK.equals(event.getName()))
		{

			int M_Product_ID = getM_Product_ID();
			
			String newText = getComponent().getText();
			String oldText = null;
			if (m_value != null)
			{
				oldText = m_mPAttribute.getDisplay(m_value);
			}
			Integer oldValue = (Integer) m_value;
			if (M_Product_ID == 0) {
				getComponent().getTextbox().setValue(oldText);
				throw new WrongValueException(getComponent().getTextbox(), "Please select a product first");
			}
			
			if (!Util.isEmpty(newText)) {
				Query query = new Query(Env.getCtx(), I_M_StorageOnHand.Table_Name, "M_StorageOnHand.M_Product_ID=? AND "
						+ "(M_AttributeSetInstance.Description=? OR M_AttributeSetInstance.Lot=? OR M_AttributeSetInstance.SerNo=?)", null);
				query.addJoinClause("JOIN M_AttributeSetInstance ON M_StorageOnHand.M_AttributeSetInstance_ID=M_AttributeSetInstance.M_AttributeSetInstance_ID");
				query.setOrderBy("M_StorageOnHand.QtyOnHand DESC");
				List<MStorageOnHand> list  = query.setParameters(M_Product_ID, newText, newText, newText).list();
				if (list.size() > 0) {
					MStorageOnHand onhand = list.get(0);
					setValue(onhand.getM_AttributeSetInstance_ID());
					if (m_GridTab != null) {
						if (gridField.getColumnName().equals("M_AttributeSetInstanceTo_ID"))
							return;
						GridField field = m_GridTab.getField("M_Locator_ID");
						if (field == null) 
							return;
						Integer currentLocator = (Integer)field.getValue();
						if (currentLocator != null) {
							for(MStorageOnHand s : list) {
								if (s.getM_Locator_ID() == currentLocator.intValue()) 
									return;
							}
						}
						if (onhand.getQtyOnHand().signum() > 0)
							m_GridTab.setValue(field, onhand.getM_Locator_ID());
						else
							m_GridTab.setValue(field, null);
					}
				} else {
					getComponent().getTextbox().setValue(oldText);
					throw new WrongValueException(getComponent().getTextbox(), Msg.getMsg(Env.getCtx(), "FindZeroRecords") + " (" + newText + ")");
				} 
			} else {
				setValue(null);
			}
												
			final int oldValueInt = oldValue == null ? 0 : oldValue.intValue ();
			final int newValueInt = getValue() == null ? 0 : ((Integer)getValue()).intValue();
			processChanges(oldValueInt, newValueInt);		
		}
		else if (Events.ON_CLICK.equals(event.getName()))
		{
			cmd_dialog();
		}
	}

	/**
	 *  Start dialog
	 */
	private void cmd_dialog()
	{
		//
		Integer oldValue = (Integer)getValue ();
		final int oldValueInt = oldValue == null ? 0 : oldValue.intValue ();
		int M_AttributeSetInstance_ID = oldValueInt;
		int M_Product_ID = getM_Product_ID();

		if (log.isLoggable(Level.CONFIG)) log.config("M_Product_ID=" + M_Product_ID 
			+ ",M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID
			+ ", AD_Column_ID=" + gridField.getAD_Column_ID());

		//	M_Product.M_AttributeSetInstance_ID = 8418
		final boolean productWindow = (gridField.getAD_Column_ID() == COLUMN_M_PRODUCT_M_ATTRIBUTESETINSTANCE_ID);		//	HARDCODED

		//
		if (!productWindow && (M_Product_ID == 0))
		{
			getComponent().setText(null);
			M_AttributeSetInstance_ID = 0;
			
			processChanges(oldValueInt, M_AttributeSetInstance_ID);
		}
		else
		{

			String tableName = gridTab.getTableName();
			boolean useNormalEditor = false;
			if (MInventoryLine.Table_Name.equals(tableName)) {
				// just for internal use, not cost adjustment or physical inventory
				int docTypeId = Env.getContextAsInt(Env.getCtx(), gridField.getWindowNo(), "C_DocType_ID");
				MDocType docType = MDocType.get(Env.getCtx(), docTypeId);
				if (! MDocType.DOCSUBTYPEINV_InternalUseInventory.equals(docType.getDocSubTypeInv())) {
					useNormalEditor = true;
				}
			}
			// multiple editor just used if the attribute set is a serial
			
			//Modificación para que el componente sea llamado sin importar si tiene o no Serial en el conjunto de atributos
			/*MProduct product = MProduct.get(Env.getCtx(), M_Product_ID);
			MAttributeSet as = product.getAttributeSet();
			if (as != null && ! as.isSerNo()) {
				useNormalEditor = true;
			}*/
			
			if (!useNormalEditor && gridField.getGridTab() != null && MMovementLine.Table_Name.equals(gridField.getGridTab().getTableName())) {
				if (gridField.getColumnName().equals(MMovementLine.COLUMNNAME_M_AttributeSetInstanceTo_ID)) {
					useNormalEditor = true;
				} else {
					int locFrom = -1;
					if (gridField.getGridTab().getValue(MMovementLine.COLUMNNAME_M_Locator_ID) != null)
						locFrom = (Integer) gridField.getGridTab().getValue(MMovementLine.COLUMNNAME_M_Locator_ID);
					int locTo = -1;
					if (gridField.getGridTab().getValue(MMovementLine.COLUMNNAME_M_LocatorTo_ID) != null)
						locTo = (Integer) gridField.getGridTab().getValue(MMovementLine.COLUMNNAME_M_LocatorTo_ID);
					if (locFrom == locTo) // inventory move changing from non-ASI to ASI - or changing ASI
						useNormalEditor = true;
				}
			}

			if (useNormalEditor || M_AttributeSetInstance_ID != 0) {
				final WPAttributeDialog vad = new WPAttributeDialog (M_AttributeSetInstance_ID, M_Product_ID, 
						m_C_BPartner_ID, productWindow, gridField.getAD_Column_ID(), m_WindowNo);
				vad.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						boolean changed = false;
						int M_AttributeSetInstance_ID = 0;
						if (vad.isChanged())
						{
							getComponent().setText(vad.getM_AttributeSetInstanceName());
							M_AttributeSetInstance_ID = vad.getM_AttributeSetInstance_ID();
							if (m_GridTab != null && !productWindow && vad.getM_Locator_ID() > 0)
							{
								if (gridField.getColumnName().equals("M_AttributeSetInstance_ID"))
									m_GridTab.setValue("M_Locator_ID", vad.getM_Locator_ID());
								else if (gridField.getColumnName().equals("M_AttributeSetInstanceTo_ID"))
									m_GridTab.setValue("M_LocatorTo_ID", vad.getM_Locator_ID());
							}					
							changed = true;
						}

						//	Set Value
						if (changed)
						{
							processChanges(oldValueInt, M_AttributeSetInstance_ID);
						}	//	change
					}
				});
			} else {
				if (! gridTab.dataSave(false)) {
					return;
				}
				BigDecimal qty = getQtyAccordingToTable();
				final WPAttributeMultipleDialog vad = new WPAttributeMultipleDialog (Env.getCtx(),
						M_AttributeSetInstance_ID, M_Product_ID, m_C_BPartner_ID,
						productWindow, gridField.getAD_Column_ID(), m_WindowNo, qty, gridTab);
				vad.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						boolean changed = false;
						int M_AttributeSetInstance_ID = 0;
						if (vad.isChanged())
						{
							getComponent().setText(vad.getM_AttributeSetInstanceName());
							M_AttributeSetInstance_ID = vad.getM_AttributeSetInstance_ID();
							if (m_GridTab != null && !productWindow && vad.getM_Locator_ID() > 0 && M_AttributeSetInstance_ID > 0)
							{
								if (gridField.getColumnName().equals("M_AttributeSetInstance_ID"))
									m_GridTab.setValue("M_Locator_ID", vad.getM_Locator_ID());
								else if (gridField.getColumnName().equals("M_AttributeSetInstanceTo_ID"))
									m_GridTab.setValue("M_LocatorTo_ID", vad.getM_Locator_ID());
								m_GridTab.dataSave(true);
							}
							changed = true;
							m_GridTab.dataRefreshAll();
						}

						//	Set Value
						if (changed)
						{							
							processChanges(oldValueInt, M_AttributeSetInstance_ID);
						}	//	change
					}
				});
			}
		}
	}   //  cmd_file

	private BigDecimal getQtyAccordingToTable() {
		BigDecimal qty;
		Object obj = null;
		String tableName = gridTab.getTableName();
		if (   MInOutLine.Table_Name.equals(tableName)
			|| MMovementLine.Table_Name.equals(tableName)) {
			obj = gridTab.getValue("MovementQty");
		} else if (MInventoryLine.Table_Name.equals(tableName)) {
			obj = gridTab.getValue("QtyInternalUse"); // just for internal use, not cost adjustment or physical inventory
		} else if (MProductionLine.Table_Name.equals(tableName)) {
			Object isEndProduct = gridTab.getValue("IsEndProduct");
			if (isEndProduct instanceof Boolean && (Boolean) isEndProduct) {
				obj = gridTab.getValue("MovementQty");
			} else {
				obj = gridTab.getValue("QtyUsed");
			}
		}

		if (obj != null && obj instanceof BigDecimal) {
			qty = (BigDecimal) obj;
		} else {
			qty = Env.ZERO;
		}

		return qty;
	}

	private void processChanges(int oldValueInt, int M_AttributeSetInstance_ID) {
		if (log.isLoggable(Level.FINEST)) log.finest("Changed M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID);
		m_value = new Object();				//	force re-query display
		if (M_AttributeSetInstance_ID == 0)
			setValue(null);
		else
			setValue(Integer.valueOf(M_AttributeSetInstance_ID));

		ValueChangeEvent vce = new ValueChangeEvent(this, gridField.getColumnName(), new Object(), getValue());
		fireValueChange(vce);
		if (M_AttributeSetInstance_ID == oldValueInt && m_GridTab != null && gridField != null)
		{
			//  force Change - user does not realize that embedded object is already saved.
			m_GridTab.processFieldChange(gridField);
		}
	}

	public String[] getEvents()
    {
        return LISTENER_EVENTS;
    }

	public void onMenu(ContextMenuEvent evt)
	{
		if (WEditorPopupMenu.ZOOM_EVENT.equals(evt.getContextEvent()))
		{
			actionZoom();
		}
		else if (WEditorPopupMenu.CHANGE_LOG_EVENT.equals(evt.getContextEvent()))
		{
			WFieldRecordInfo.start(gridField);
		}
	}

	public void actionZoom()
	{
	   	AEnv.actionZoom(m_mPAttribute, getValue());
	}

	@Override
	public boolean isReadWrite() {
		return getComponent().getButton().isEnabled();
	}

	@Override
	public void setReadWrite(boolean readWrite) {
		getComponent().setEnabled(readWrite);		
	}

	@Override
	public void setTableEditor(boolean b) {
		super.setTableEditor(b);
		getComponent().setTableEditorMode(b);
	}
	
	private int getM_Product_ID() {
		int M_Product_ID = 0;
		int M_ProductBOM_ID = 0;
		
		if (m_GridTab != null) {
			M_Product_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, m_GridTab.getTabNo(), "M_Product_ID");
			M_ProductBOM_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, m_GridTab.getTabNo(), "M_ProductBOM_ID");
		} else if (gridField==null || gridField.getVO().ctx.getProperty("find.window.context") == null){
			M_Product_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, "M_Product_ID");
			M_ProductBOM_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, "M_ProductBOM_ID");
		}

		if (M_ProductBOM_ID != 0)	//	Use BOM Component
			M_Product_ID = M_ProductBOM_ID;
		
		return M_Product_ID;
	}
}
