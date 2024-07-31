/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package com.trekglobal.MultipleASIEditor.editor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.Combobox;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Datebox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.Dialog;
import org.adempiere.webui.window.WPAttributeInstance;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_Activity;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.MActivity;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeUse;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MColumn;
import org.compiere.model.MDocType;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MLot;
import org.compiere.model.MLotCtl;
import org.compiere.model.MMovementLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProduction;
import org.compiere.model.MProductionLine;
import org.compiere.model.MProject;
import org.compiere.model.MProjectLine;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.MSerNoCtl;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.impl.InputElement;

/**
 *  Product Attribute Set Product/Instance Dialog Editor.
 * 	Called from VPAttribute.actionPerformed
 *
 *  @author Jorg Janke
 *  
 *  ZK Port
 *  @author Low Heng Sin
 */
public class WPAttributeMultipleDialog extends Window implements EventListener<Event>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1735658692497873595L;
	private Properties m_ctx;

	/**
	 *	Product Attribute Instance Dialog
	 * @param ctx 
	 *	@param M_AttributeSetInstance_ID Product Attribute Set Instance id
	 * 	@param M_Product_ID Product id
	 * 	@param C_BPartner_ID b partner
	 * 	@param productWindow this is the product window (define Product Instance)
	 * 	@param AD_Column_ID column
	 * 	@param WindowNo window
	 * @param qty 
	 * @param gridTab 
	 */
	public WPAttributeMultipleDialog (Properties ctx, int M_AttributeSetInstance_ID, 
		int M_Product_ID, int C_BPartner_ID, 
		boolean productWindow, int AD_Column_ID, int WindowNo, BigDecimal qty, GridTab gridTab)
	{
		super ();
		m_ctx = ctx;
		this.setTitle("*** " + Msg.translate(m_ctx, "M_AttributeSetInstance_ID") + " ***");
		ZKUpdateUtil.setWidth(this, "500px");
		this.setSclass("popup-dialog");
//		this.setHeight("600px");
		this.setBorder("normal");
		this.setShadow(true);
		this.setSizable(true);
		
		if (log.isLoggable(Level.CONFIG)) log.config("M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID 
			+ ", M_Product_ID=" + M_Product_ID
			+ ", C_BPartner_ID=" + C_BPartner_ID
			+ ", ProductW=" + productWindow + ", Column=" + AD_Column_ID);		 
		m_WindowNo = SessionManager.getAppDesktop().registerWindow(this);
		m_M_AttributeSetInstance_ID = M_AttributeSetInstance_ID;
		m_M_Product_ID = M_Product_ID;
		m_C_BPartner_ID = C_BPartner_ID;
		m_productWindow = productWindow;
		m_AD_Column_ID = AD_Column_ID;
		m_WindowNoParent = WindowNo;
		m_QtyRequired = qty;
		m_gridTab = gridTab;

		//get columnName from ad_column
		MColumn column = MColumn.get(m_ctx, m_AD_Column_ID);
		m_columnName = column.getColumnName();
		if (m_columnName == null || m_columnName.trim().length() == 0) 
		{
			//fallback
			m_columnName = "M_AttributeSetInstance_ID";
		}
		
		try
		{
			init();
		}
		catch(Exception ex)
		{
			log.log(Level.SEVERE, "VPAttributeDialog" + ex);
		}
		//	Dynamic Init
		if (!initAttributes ())
		{
			dispose();
			return;
		}
		AEnv.showCenterScreen(this);
	}	//	VPAttributeDialog

	private int						m_WindowNo;
	private MAttributeSetInstance	m_masi;
	private int 					m_M_AttributeSetInstance_ID;
	private int 					m_M_Locator_ID;
	private int 					m_M_Product_ID;
	private int						m_C_BPartner_ID;
	private int						m_AD_Column_ID;
	private int						m_WindowNoParent;
	private GridTab					m_gridTab;
	private BigDecimal				m_QtyRequired;
	/**	Enter Product Attributes		*/
	private boolean					m_productWindow = false;
	/**	Change							*/
	private boolean					m_changed = false;
	
	private static final CLogger		log = CLogger.getCLogger(WPAttributeMultipleDialog.class);
	/** Row Counter					*/
	private int						m_row = 0;
	/** List of Editors				*/
	private ArrayList<HtmlBasedComponent>		m_editors = new ArrayList<HtmlBasedComponent>();
	private ArrayList<HtmlBasedComponent>		m_FocusEditors = new ArrayList<HtmlBasedComponent>();
	/** Length of Instance value (40)	*/
	//private static final int		INSTANCE_VALUE_LENGTH = 40;

	private Button		bSelect = new Button(); 
	private Button		bNext = new Button(); 
	//	Lot
	private Textbox fieldLotString = new Textbox();
	private Listbox fieldLot = new Listbox();
	private Button bLot = new Button(Util.cleanAmp(Msg.getMsg (Env.getCtx(), "New")) + " " + Msg.translate(Env.getCtx(), "Lot"));
	//	Lot Popup
	Menupopup 					popupMenu = new Menupopup();
	private Menuitem 			mZoom;
	//	Ser No
	private Textbox fieldSerNo = new Textbox();
	private Button bSerNo = new Button(Util.cleanAmp(Msg.getMsg (Env.getCtx(), "New")) + " " + Msg.translate(Env.getCtx(), "SerNo"));
	private Button bAllSerNo = new Button(Util.cleanAmp(Msg.getMsg (Env.getCtx(), "All")) + " " + Msg.translate(Env.getCtx(), "SerNo"));
	private boolean m_isJustSerial = false;
	private Row m_rowSerNo;
	//	Date
	private Datebox fieldGuaranteeDate = new Datebox();
	//
	private Textbox fieldDescription = new Textbox(); //TODO: set length to 20
	//
	private Borderlayout mainLayout = new Borderlayout();
	private Panel northPanel = new Panel();
	private Grid northLayout = new Grid();
	private Panel centerPanel = new Panel();
	private ConfirmPanel confirmPanel = new ConfirmPanel (true);
	private WListbox multipleASITable = ListboxFactory.newDataTable();
	private Vector<Vector<Object>> multipleASIData = new Vector<Vector<Object>>();
	private Vector<String> multipleASIColumnNames = new Vector<String>();
	private Vector<MAttributeSetInstance> m_ASIs = new Vector<MAttributeSetInstance>();
	private Vector<MAttributeSetInstance> m_newASIs = new Vector<MAttributeSetInstance>();
	private Label statusBar = new Label();

	private String m_columnName = null;
	private int multipleASIidxCol = 0;
	
	private WTableDirEditor activityEditor;
	
	private Hashtable<String, Object> htIngredient = new Hashtable<String, Object>();
	private Object obj = new Object();

	/**
	 *	Layout
	 * 	@throws Exception
	 */
	private void init () throws Exception
	{
		mainLayout.setParent(this);
		ZKUpdateUtil.setHflex(mainLayout, "1");
		ZKUpdateUtil.setVflex(mainLayout, "min");

		North north = new North();
		north.setSclass("dialog-content");
		north.setParent(mainLayout);
		ZKUpdateUtil.setVflex(northPanel, "1");
		ZKUpdateUtil.setHflex(northPanel, "1");
		north.appendChild(northPanel);

		Center center = new Center();
		center.setSclass("dialog-content");
		center.setParent(mainLayout);
		ZKUpdateUtil.setVflex(centerPanel, "1");
		ZKUpdateUtil.setHflex(centerPanel, "1");
		center.appendChild(centerPanel);
		centerPanel.appendChild(multipleASITable);
		ZKUpdateUtil.setWidth(multipleASITable, "99%");
		ZKUpdateUtil.setHeight(multipleASITable, "200px");

		South south = new South();
		south.setSclass("dialog-footer");
		south.setParent(mainLayout);
		confirmPanel.appendChild(statusBar);
		statusBar.setValue(Msg.getMsg(m_ctx, "Quantity") + " = " + Math.min(Math.max(1, multipleASIData.size()), m_QtyRequired.intValue()) + " / " + m_QtyRequired);
		south.appendChild(confirmPanel);
		
		northPanel.appendChild(northLayout);
		northLayout.setOddRowSclass("even");
		//
		confirmPanel.addActionListener(Events.ON_CLICK, this);
		if (m_QtyRequired.intValue() != 0)
		{
			bNext.setEnabled(false);
			confirmPanel.getButton("Ok").setEnabled(false);
		}
	}	//	init

	/**
	 *	Dyanmic Init.
	 *  @return true if initialized
	 */
	private boolean initAttributes ()
	{
		Vector<Object> line = new Vector<Object>();
		multipleASIidxCol  = 0;
		multipleASITable.clear();

		Columns columns = new Columns();
		columns.setParent(northLayout);
		
		Column column = new Column();
		column.setParent(columns);
		ZKUpdateUtil.setWidth(column, "30%");
		
		column = new Column();
		column.setParent(columns);
		ZKUpdateUtil.setWidth(column, "70%");
		
		Rows rows = new Rows();
		rows.setParent(northLayout);
		
		if (m_M_Product_ID == 0 && !m_productWindow)
			return false;
		
		MAttributeSet as = null;
		
		int M_AttributeSet_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, "M_AttributeSet_ID");
		if (m_M_Product_ID != 0 && M_AttributeSet_ID == 0)
		{
			//	Get Model
			m_masi = MAttributeSetInstance.get(m_ctx, m_M_AttributeSetInstance_ID, m_M_Product_ID);
			if (m_masi == null)
			{
				log.severe ("No Model for M_AttributeSetInstance_ID=" + m_M_AttributeSetInstance_ID + ", M_Product_ID=" + m_M_Product_ID);
				return false;
			}
			Env.setContext(m_ctx, m_WindowNo, "M_AttributeSet_ID", m_masi.getM_AttributeSet_ID());
	
			//	Get Attribute Set
			as = m_masi.getMAttributeSet();
			m_ASIs.add(m_masi);
			m_newASIs.add(m_masi);
		}
		else 
		{
			m_masi = new MAttributeSetInstance (m_ctx, m_M_AttributeSetInstance_ID, M_AttributeSet_ID, null);
			as = m_masi.getMAttributeSet();
		}
		
		//	Product has no Attribute Set
		if (as == null)		
		{
			Dialog.error(m_WindowNo, "PAttributeNoAttributeSet");
			return false;
		}
		//	Product has no Instance Attributes
		if (!m_productWindow && !as.isInstanceAttribute())
		{
			Dialog.error(m_WindowNo, "PAttributeNoInstanceAttribute");
			return false;
		}

		//	SerNo
		if (!m_productWindow && as.isSerNo())
		{
			Row row = new Row();
			row.setParent(rows);
			m_row++;
			Label label = new Label (Msg.translate(m_ctx, "SerNo"));
			row.appendChild(label);
			row.appendChild(fieldSerNo);
			ZKUpdateUtil.setHflex(fieldSerNo, "1");
			fieldSerNo.setText(m_masi.getSerNo());
			multipleASIColumnNames.add(label.getValue());
			multipleASITable.setColumnClass(multipleASIidxCol++, String.class, true);
			line.add(m_masi.getSerNo());

			//	New SerNo Button
			if (m_masi.getMAttributeSet().getM_SerNoCtl_ID() != 0)
			{
				if (MRole.getDefault().isTableAccess(MSerNoCtl.Table_ID, false)
					&& !m_masi.isExcludeSerNo(m_AD_Column_ID, Env.isSOTrx(m_ctx, m_WindowNoParent)))
				{
					row = new Row();
					row.setParent(rows);
					m_row++;
					row.appendChild(bSerNo);
					bSerNo.addEventListener(Events.ON_CLICK, this);
					LayoutUtils.addSclass("txt-btn", bSerNo);
					m_isJustSerial = true;
					m_rowSerNo = row;
				}
			}
		}	//	SerNo

		//	Lot
		if (!m_productWindow && as.isLot())
		{
			m_isJustSerial = false;
			Row row = new Row();
			row.setParent(rows);
			m_row++;
			Label label = new Label (Msg.translate(m_ctx, "Lot"));
			row.appendChild(label);
			row.appendChild(fieldLotString);
			ZKUpdateUtil.setHflex(fieldLotString, "1");
			fieldLotString.setText (m_masi.getLot());
			multipleASIColumnNames.add(label.getValue());
			multipleASITable.setColumnClass(multipleASIidxCol++, String.class, true);
			line.add(m_masi.getLot());
			//	M_Lot_ID
		//	int AD_Column_ID = 9771;	//	M_AttributeSetInstance.M_Lot_ID
		//	fieldLot = new VLookup ("M_Lot_ID", false,false, true, 
		//		MLookupFactory.get(m_ctx, m_WindowNo, 0, AD_Column_ID, DisplayType.TableDir));
			String sql = "SELECT M_Lot_ID, Name "
				+ "FROM M_Lot l "
				+ "WHERE EXISTS (SELECT M_Product_ID FROM M_Product p "
					+ "WHERE p.M_AttributeSet_ID=" + m_masi.getM_AttributeSet_ID()
					+ " AND p.M_Product_ID=l.M_Product_ID)";
			fieldLot = new Listbox();
			fieldLot.setMold("select");
			KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql, true);
			for (KeyNamePair pair : keyNamePairs) {
				fieldLot.appendItem(pair.getName(), pair.getKey());
			}
						
			label = new Label (Msg.translate(m_ctx, "M_Lot_ID"));
			row = new Row();
			row.setParent(rows);
			m_row++;
			row.appendChild(label);
			row.appendChild(fieldLot);
			ZKUpdateUtil.setHflex(fieldLot, "1");
			if (m_masi.getM_Lot_ID() != 0)
			{
				for (int i = 1; i < fieldLot.getItemCount(); i++)
				{
					ListItem pp = fieldLot.getItemAtIndex(i);
					if ((Integer)pp.getValue() == m_masi.getM_Lot_ID())
					{
						fieldLot.setSelectedIndex(i);
						fieldLotString.setReadonly(true);
						break;
					} 
				}
			}
			fieldLot.addEventListener(Events.ON_SELECT, this);
			//	New Lot Button
			if (m_masi.getMAttributeSet().getM_LotCtl_ID() != 0)
			{
				if (MRole.getDefault().isTableAccess(MLot.Table_ID, false)
					&& MRole.getDefault().isTableAccess(MLotCtl.Table_ID, false)
					&& !m_masi.isExcludeLot(m_AD_Column_ID, Env.isSOTrx(m_ctx, m_WindowNoParent)))
				{
					row = new Row();
					row.setParent(rows);
					m_row++;
					row.appendChild(bLot);
					bLot.addEventListener(Events.ON_CLICK, this);
					LayoutUtils.addSclass("txt-btn", bLot);
				}
			}
			//	Popup 
//			fieldLot.addMouseListener(new VPAttributeDialog_mouseAdapter(this));    //  popup
			mZoom = new Menuitem(Msg.getMsg(m_ctx, "Zoom"), ThemeManager.getThemeResource("images/Zoom16.png"));
			mZoom.addEventListener(Events.ON_CLICK, this);
			popupMenu.appendChild(mZoom);
			this.appendChild(popupMenu);
		}	//	Lot

		//	GuaranteeDate
		if (!m_productWindow && as.isGuaranteeDate())
		{
			m_isJustSerial = false;
			Row row = new Row();
			row.setParent(rows);
			m_row++;
			Label label = new Label (Msg.translate(m_ctx, "GuaranteeDate"));
			if (m_M_AttributeSetInstance_ID == 0)
				fieldGuaranteeDate.setValue(m_masi.getGuaranteeDate(true));
			else
				fieldGuaranteeDate.setValue(m_masi.getGuaranteeDate());
			row.appendChild(label);
			row.appendChild(fieldGuaranteeDate);			
			multipleASIColumnNames.add(label.getValue());
			multipleASITable.setColumnClass(multipleASIidxCol++, Timestamp.class, true);
			line.add(m_masi.getGuaranteeDate());
		}	//	GuaranteeDate
		
		//	Optional activity
		if (!m_productWindow && hasActivityColumn())
		{
			m_isJustSerial = false;
			Row row = new Row();
			row.setParent(rows);
			m_row++;
			
			MLookup activityLookup = null;
			try {
				activityLookup = MLookupFactory.get(Env.getCtx(), m_WindowNo,
						MColumn.getColumn_ID(MActivity.Table_Name, MActivity.COLUMNNAME_C_Activity_ID),
						DisplayType.TableDir, Env.getLanguage(Env.getCtx()), MActivity.COLUMNNAME_C_Activity_ID, 0, false, null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			activityEditor = new WTableDirEditor(activityLookup, Msg.getElement(m_ctx, I_C_Activity.COLUMNNAME_C_Activity_ID), 
					null, false, false, true);
			
			Label label = new Label (Msg.translate(m_ctx, I_C_Activity.COLUMNNAME_C_Activity_ID));
			if (m_M_AttributeSetInstance_ID == 0)
				activityEditor.setValue(0);
			else
				activityEditor.setValue(m_masi.get_Value(I_C_Activity.COLUMNNAME_C_Activity_ID));
			row.appendChild(label);
			activityEditor.fillHorizontal();
			row.appendChild(activityEditor.getComponent());			
			multipleASIColumnNames.add(label.getValue());
			multipleASITable.setColumnClass(multipleASIidxCol++, String.class, true);
			line.add(activityEditor.getDisplay());			
		}	//	activity

		//	Show Product Attributes
		if (! m_productWindow)
		{
			initDefaultValue(as);
			
			//	All Attributes
			MAttribute[] attributes = as.getMAttributes (true);
			if (log.isLoggable(Level.FINE)) log.fine ("Instance Attributes=" + attributes.length);
			for (int i = 0; i < attributes.length; i++) {
				m_isJustSerial = false;
				addAttributeLine (rows, attributes[i], false, false, line);
			}
		}

		if (m_row == 0)
		{
			Dialog.error(m_WindowNo, "PAttributeNoInfo");
			return false;
		}

		//	Attribute Set Instance Description
		Label label = new Label (Msg.translate(m_ctx, "Description"));
//		label.setLabelFor(fieldDescription);
		fieldDescription.setText(m_masi.getDescription());
		fieldDescription.setReadonly(true);
		Row row = new Row();
		row.setParent(rows);
		row.appendChild(label);
		row.appendChild(fieldDescription);
		ZKUpdateUtil.setHflex(fieldDescription, "1");

		if (! m_productWindow)
		{
			row = new Row();
			bNext.setLabel(Msg.getMsg(m_ctx, "Next"));
			bNext.setImage(ThemeManager.getThemeResource("images/Next16.png"));
			row.appendChild(bNext);
			ZKUpdateUtil.setHflex(bNext, "1");

			bSelect.setLabel(Msg.getMsg(m_ctx, "SelectExisting"));
			bSelect.setImage(ThemeManager.getThemeResource("images/PAttribute16.png"));
			bSelect.addEventListener(Events.ON_CLICK, this);
			row.appendChild(bSelect);
			ZKUpdateUtil.setHflex(bSelect, "1");
			rows.appendChild(row);
		}

		if (m_isJustSerial && m_QtyRequired.signum() != 0) {
			m_rowSerNo.appendChild(bAllSerNo);
			bAllSerNo.addEventListener(Events.ON_CLICK, this);
			LayoutUtils.addSclass("txt-btn", bAllSerNo);
		}

		//  Set Model
		multipleASIData.add(line);
		ListModelTable model = new ListModelTable(multipleASIData);
		multipleASITable.setData(model, multipleASIColumnNames);

		// set listener for editors
		if (!m_productWindow && as.isSerNo()) {
			fieldSerNo.addEventListener(Events.ON_CHANGE, this);
			fieldSerNo.addEventListener(Events.ON_OK, this);
			m_FocusEditors.add(fieldSerNo);
		}
		if (!m_productWindow && as.isLot()) {
			fieldLotString.addEventListener(Events.ON_CHANGE, this);
			fieldLotString.addEventListener(Events.ON_OK, this);
			m_FocusEditors.add(fieldLotString);
		}
		if (!m_productWindow && as.isGuaranteeDate()) {
			fieldGuaranteeDate.addEventListener(Events.ON_CHANGE, this);
			fieldGuaranteeDate.addEventListener(Events.ON_OK, this);
			m_FocusEditors.add(fieldGuaranteeDate);
		}
		
		if (!m_productWindow && activityEditor != null) {
			activityEditor.getComponent().addEventListener(Events.ON_SELECT, this);
			activityEditor.getComponent().addEventListener(Events.ON_OK, this);
			m_FocusEditors.add(activityEditor.getComponent());
		}
		
		for (int i = 0; i < m_editors.size(); i++)
		{
			HtmlBasedComponent editor = m_editors.get(i);
			if (editor instanceof NumberBox) {
				editor = ((NumberBox) editor).getDecimalbox();
			}
			if (editor instanceof Listbox) {
				editor.addEventListener(Events.ON_SELECT, this);
			} else {
				editor.addEventListener(Events.ON_CHANGE, this);
			}
			editor.addEventListener(Events.ON_OK, this);
			m_FocusEditors.add(editor);
		}

		bNext.addEventListener(Events.ON_CLICK, this);
		return true;
	}	//	initAttribute
	
	private void initDefaultValue(MAttributeSet as)
	{
		String tableName = m_gridTab.getTableName();
		if (MProductionLine.Table_Name.equals(tableName) || MProjectLine.Table_Name.equals(tableName))
		{
			if (m_gridTab.getValueAsBoolean("IsEndProduct"))
			{
				ArrayList<MAttributeUse> copyAttributeUseList = new ArrayList<MAttributeUse>();
				MAttributeUse[] attributeUses = as.getMAttributeUse();
				for (MAttributeUse attributeUse : attributeUses)
				{
					if (attributeUse.get_ValueAsBoolean("IsCopyToEndProduct"))
						copyAttributeUseList.add(attributeUse);
				}
				
				if (!copyAttributeUseList.isEmpty())
				{
					PO[] lines = null;
					
					if (MProductionLine.Table_Name.equals(tableName))
					{
						Integer M_Production_ID = (Integer) m_gridTab.getValue(MProductionLine.COLUMNNAME_M_Production_ID);
						if (M_Production_ID != null && M_Production_ID.intValue() > 0)
						{	
							MProduction production = new MProduction(Env.getCtx(), M_Production_ID, null);
							lines = production.getLines();
						}
					}
					else if (MProjectLine.Table_Name.equals(tableName))
					{
						Integer M_Project_ID = (Integer) m_gridTab.getValue(MProjectLine.COLUMNNAME_C_Project_ID);
						if (M_Project_ID != null && M_Project_ID.intValue() > 0)
						{
							MProject project = new MProject(Env.getCtx(), M_Project_ID, null);
							lines = project.getLines();
						}
					}
					
					if (lines != null)
					{						
						for (PO line : lines)
						{
							if (line.get_ValueAsInt("M_AttributeSetInstance_ID") > 0 && !line.get_ValueAsBoolean("IsEndProduct"))
							{
								MAttributeSetInstance asi = new MAttributeSetInstance(Env.getCtx(), line.get_ValueAsInt("M_AttributeSetInstance_ID"), null);
								if (asi.getM_AttributeSet_ID() > 0)
								{
									MAttributeSet mas = asi.getMAttributeSet();
									if (mas != null)
									{
										MAttribute[] attributes = mas.getMAttributes(true);
										for (MAttribute attribute : attributes)
										{
											MAttributeInstance instance = attribute.getMAttributeInstance(asi.getM_AttributeSetInstance_ID());
											if (instance == null)
												continue;
											
											boolean found = false;
											for (MAttributeUse attributeUse : copyAttributeUseList)
											{
												if (attributeUse.getM_Attribute_ID() == attribute.getM_Attribute_ID())
												{
													found = true;
													break;
												}
											}
											
											if (!found)
												continue;
											
											if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
											{
												MAttributeValue[] values = attribute.getMAttributeValues();
												for (MAttributeValue value : values)
												{
													if (value != null && value.getM_AttributeValue_ID() == instance.getM_AttributeValue_ID())
													{
														Object data = (Object) htIngredient.get(attribute.getName());
														if (data != null)
														{
															if (data.equals(obj))
																break;
															
															if (!((String) data).equals(value.getName()))
																htIngredient.put(attribute.getName(), obj);
															else
																htIngredient.put(attribute.getName(), value.getName());
														}
														else
															htIngredient.put(attribute.getName(), value.getName());
														break;
													}
												}													
											}
											else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attribute.getAttributeValueType()))
											{
												Object data = (Object) htIngredient.get(attribute.getName());
												if (data != null)
												{
													if (data.equals(obj))
														break;
													
													if (((BigDecimal) data).compareTo(instance.getValueNumber()) != 0)
														htIngredient.put(attribute.getName(), obj);
													else
														htIngredient.put(attribute.getName(), instance.getValueNumber());
												}
												else if (instance.getValueNumber() != null)
													htIngredient.put(attribute.getName(), instance.getValueNumber());													
											}
											else
											{
												Object data = (Object) htIngredient.get(attribute.getName());
												if (data != null)
												{
													if (data.equals(obj))
														break;
													
													if (!((String) data).equals(instance.getValue()))
														htIngredient.put(attribute.getName(), obj);
													else
														htIngredient.put(attribute.getName(), instance.getValue());
												}
												else if (instance.getValue() != null)
													htIngredient.put(attribute.getName(), instance.getValue());
											}												
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 	Add Attribute Line
	 *	@param attribute attribute
	 * 	@param product product level attribute
	 * 	@param readOnly value is read only
	 * @param line 
	 */
	private void addAttributeLine (Rows rows, MAttribute attribute, boolean product, boolean readOnly, Vector<Object> line)
	{
		if (log.isLoggable(Level.FINE)) log.fine(attribute + ", Product=" + product + ", R/O=" + readOnly);
		
		m_row++;
		Label label = new Label (attribute.getName());
		if (product)
			label.setStyle("font-weight: bold");
			
		if (attribute.getDescription() != null)
			label.setTooltiptext(attribute.getDescription());
		
		Row row = rows.newRow();
		row.appendChild(label.rightAlign());
		//
		multipleASIColumnNames.add(attribute.getName());
		
		if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
		{
			MAttributeValue[] values = attribute.getMAttributeValues();	//	optional = null
			Listbox editor = new Listbox();
			editor.setMold("select");
			for (MAttributeValue value : values) 
			{
				ListItem item = new ListItem(value != null ? value.getName() : "", value);
				editor.appendChild(item);
			}
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
			row.appendChild(editor);
			editor.setSelectedIndex(-1);
			ZKUpdateUtil.setHflex(editor, "1");
			setListAttribute(attribute, editor, line);
			multipleASITable.setColumnClass(multipleASIidxCol++, String.class, true);
		}
		else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attribute.getAttributeValueType()))
		{
			NumberBox editor = new NumberBox(false);
			setNumberAttribute(attribute, editor, line);
			row.appendChild(editor);
			ZKUpdateUtil.setHflex(editor, "1");
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
			multipleASITable.setColumnClass(multipleASIidxCol++, BigDecimal.class, true);
		}
		else	//	Text Field
		{
			Textbox editor = new Textbox();
			setStringAttribute(attribute, editor, line);
			row.appendChild(editor);
			ZKUpdateUtil.setHflex(editor, "1");
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
			multipleASITable.setColumnClass(multipleASIidxCol++, String.class, true);
		}
	}	//	addAttributeLine

	private void setStringAttribute(MAttribute attribute, Textbox editor, Vector<Object> line) {
		MAttributeInstance instance = attribute.getMAttributeInstance (m_M_AttributeSetInstance_ID);
		if (instance != null)
			editor.setText(instance.getValue());
		else {
			Object data = htIngredient.get(attribute.getName());
			if (data != null && !data.equals(obj))
				editor.setText((String) data);
		}
			
		line.add(editor.getText());
	}

	private void setNumberAttribute(MAttribute attribute, NumberBox editor, Vector<Object> line) {
		MAttributeInstance instance = attribute.getMAttributeInstance (m_M_AttributeSetInstance_ID);
		if (instance != null)
			editor.setValue(instance.getValueNumber());
		else {
			Object data = htIngredient.get(attribute.getName());
			if (data != null && !data.equals(obj))
				editor.setValue(data);
			else
				editor.setValue(Env.ZERO);
		}
		line.add(editor.getValue());
	}

	private void setListAttribute(MAttribute attribute, Listbox editor, Vector<Object> line) {
		boolean found = false;
		MAttributeInstance instance = attribute.getMAttributeInstance (m_M_AttributeSetInstance_ID);
		MAttributeValue[] values = attribute.getMAttributeValues();	//	optional = null
		if (instance != null)
		{
			for (int i = 0; i < values.length; i++)
			{
				if (values[i] != null && values[i].getM_AttributeValue_ID () == instance.getM_AttributeValue_ID ())
				{
					editor.setSelectedIndex (i);
					line.add(editor.getSelectedItem().getValue());
					found = true;
					break;
				}
			}
			if (found ){
				if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + attribute.getName() + " #" + values.length + " - found: " + instance);
			} else {
				line.add("");
				log.warning("Attribute=" + attribute.getName() + " #" + values.length + " - NOT found: " + instance);
			}
		}	//	setComboBox
		else
		{
			Object data = htIngredient.get(attribute.getName());
			if (data != null && !data.equals(obj))
			{
				for (int i = 0; i < values.length; i++)
				{
					if (values[i] != null && values[i].getName().equals(data))
					{
						editor.setSelectedIndex(i);
						line.add(editor.getSelectedItem().getValue());
						found = true;
						break;
					}
				}
				
				if (!found)
					line.add("");
			}
			else
			{
				ListItem item = editor.getSelectedItem();
				line.add(item != null ? item.getValue() : "");
				if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + attribute.getName() + " #" + values.length + " no instance");	
			}			
		}
	}

	/**
	 *	dispose
	 */
	public void dispose()
	{
		Env.clearWinContext(m_WindowNo);
		//
		if (isChanged())
		{
			Env.setContext(m_ctx, m_WindowNoParent, Env.TAB_INFO, m_columnName, 
				String.valueOf(m_M_AttributeSetInstance_ID));
			
			if (m_M_Locator_ID > 0)
			{
				if (m_columnName.equals("M_AttributeSetInstance_ID"))
					Env.setContext(m_ctx, m_WindowNoParent, Env.TAB_INFO, "M_Locator_ID", 
							String.valueOf(m_M_Locator_ID));
				else if (m_columnName.equals("M_AttributeSetInstanceTo_ID"))
					Env.setContext(m_ctx, m_WindowNoParent, Env.TAB_INFO, "M_LocatorTo_ID", 
							String.valueOf(m_M_Locator_ID));
			}
			else
			{
				if (m_columnName.equals("M_AttributeSetInstance_ID"))
				{
					String context = m_WindowNoParent+"|"+Env.TAB_INFO+"|"+"M_Locator_ID";
					m_ctx.remove(context);
				}
				else if (m_columnName.equals("M_AttributeSetInstanceTo_ID"))
				{
					String context = m_WindowNoParent+"|"+Env.TAB_INFO+"|"+"M_LocatorTo_ID";
					m_ctx.remove(context);
				}
			}
		}
		//
		this.detach();
	}	//	dispose

	private Object m_justProcessedChange;
	private long m_lastSavedTime;
	public void onEvent(Event e) throws Exception 
	{
		//	Select Instance
		if (e.getTarget() == bSelect)
		{
			cmd_select();				
		}
		//	Next
		else if (e.getTarget() == bNext)
		{
			cmd_next();				
		}
		//	Select Lot from existing
		else if (e.getTarget() == fieldLot)
		{
			ListItem pp = fieldLot.getSelectedItem();
			if (pp != null && (Integer)pp.getValue() != -1)
			{
				fieldLotString.setText(pp.getLabel());
				fieldLotString.setReadonly(true);
				m_masi.setM_Lot_ID((Integer)pp.getValue());
			}
			else
			{
				fieldLotString.setReadonly(false);
				m_masi.setM_Lot_ID(0);
			}
			Events.sendEvent(Events.ON_CHANGE, fieldLotString, fieldLotString.getText());
		}
		//	Create New Lot
		else if (e.getTarget() == bLot)
		{
			KeyNamePair pp = m_masi.createLot(m_M_Product_ID);
			if (pp != null)
			{
				ListItem item = new ListItem(pp.getName(), pp.getKey());
				fieldLot.appendChild(item);
				fieldLot.setSelectedItem(item);
				fieldLotString.setText (m_masi.getLot());
				fieldLotString.setReadonly(true);
				Events.sendEvent(Events.ON_CHANGE, fieldLotString, fieldLotString.getText());
			}
		}
		//	Create New SerNo
		else if (e.getTarget() == bSerNo)
		{
			fieldSerNo.setText(m_masi.getSerNo(true));
			Events.sendEvent(Events.ON_CHANGE, fieldSerNo, fieldSerNo.getText());
		}
		//	Create All SerNo
		else if (e.getTarget() == bAllSerNo)
		{
			int qtyCaptured = multipleASIData.size();
			int qtyRequired = m_QtyRequired.abs().intValue();
			for (int idx = qtyCaptured; idx <= qtyRequired; idx++) {
				fieldSerNo.setText(m_masi.getSerNo(true));
				processEditor(true, 0, true); // bAllSerNo is just enabled when there is JUST serial editor
			}
			multipleASITable.clearTable();
			ListModelTable model = new ListModelTable(multipleASIData);
			multipleASITable.setData(model, multipleASIColumnNames);
			m_justProcessedChange = e.getTarget();
			if (saveDocumentLines())
				dispose();
		}
		//	OK
		else if (e.getTarget().getId().equals("Ok"))
		{
			if (saveSelection())
			{
				if (saveDocumentLines())
					dispose();
			}
		}
		//	Cancel
		else if (e.getTarget().getId().equals("Cancel"))
		{
			// on cancel delete the ASI records created during this session
			for (MAttributeSetInstance asi : m_newASIs) {
				if (asi.getM_AttributeSetInstance_ID() > 0) {
					asi.delete(true);
				}
			}
			m_changed = false;
			m_M_AttributeSetInstance_ID = 0;
			m_M_Locator_ID = 0;
			dispose();
		}
		//	Zoom M_Lot
		else if (e.getTarget() == mZoom)
		{
			cmd_zoom();
		}
		else if ((Events.ON_OK.equals(e.getName()) || Events.ON_CHANGE.equals(e.getName()) || Events.ON_SELECT.equals(e.getName()))
				&& e.getTarget() instanceof HtmlBasedComponent)
		{
			if (Events.ON_OK.equals(e.getName())) {
				if (e.getTarget() == m_justProcessedChange) {
					// avoid double processing of OK and CHANGE on the same field
					m_justProcessedChange = null;
					if (log.isLoggable(Level.INFO)) log.info("NOT reprocessing " + e.getTarget());
					return;
				}
			}
			if (log.isLoggable(Level.INFO)) log.info("Processing " + e.getTarget());
			if (e.getTarget() == fieldSerNo) {
				// Validate unique serial, if serial exists get the ASI from that serial
				int existingSerialID = DB.getSQLValue(null,
						"SELECT M_AttributeSetInstance_ID FROM M_AttributeSetInstance WHERE SerNo=? AND M_AttributeSet_ID=? AND M_AttributeSetInstance_ID IN (" +
						"SELECT M_AttributeSetInstance_ID FROM M_StorageOnHand WHERE M_Product_ID = ?)",
						fieldSerNo.getText(), m_masi.getM_AttributeSet_ID(), m_M_Product_ID);
				if (existingSerialID > 0) {
					m_M_AttributeSetInstance_ID = existingSerialID;
					m_changed = true;
					loadExistingASI();
					return;
				}
			}
			boolean isLast = false;
			int idxEditor = -1;
			for (int i = 0; i < m_FocusEditors.size(); i++) {
				if (e.getTarget() == m_FocusEditors.get(i)) {
					idxEditor = i;
					if (i == m_FocusEditors.size()-1)
						isLast = true;
					break;
				}
			}
			if (idxEditor >= 0) {
				processEditor(isLast, idxEditor, false);
				m_justProcessedChange = e.getTarget();
				multipleASITable.clearTable();
				ListModelTable model = new ListModelTable(multipleASIData);
				multipleASITable.setData(model, multipleASIColumnNames);
				return;
			} else {
				log.log(Level.SEVERE, "not managed change - " + e);
			}
		}
		else
			log.log(Level.SEVERE, "not found - " + e);
		m_justProcessedChange = null;
	}	//	actionPerformed

	private synchronized void processEditor(boolean isLast, int idxEditor, boolean fromAllButton) {
		// field changed:Events
		// - fill the corresponding line
		Vector<Object> actualLine = multipleASIData.get(multipleASIData.size()-1);
		HtmlBasedComponent editor = m_FocusEditors.get(idxEditor);
		if (editor instanceof Textbox) {
			actualLine.set(idxEditor, ((Textbox)editor).getText());
		} else if (editor instanceof Datebox) {
			actualLine.set(idxEditor, ((Datebox)editor).getValue());
		} else if (editor instanceof Decimalbox) {
			actualLine.set(idxEditor, ((Decimalbox)editor).getValue());
		} else if (editor instanceof Listbox) {
			ListItem item = ((Listbox)editor).getSelectedItem();
			if (item != null)
				actualLine.set(idxEditor, item.getValue());
		} else if (editor instanceof Combobox) {
			Comboitem selectedItem = ((Combobox) editor).getSelectedItem();
			if (selectedItem != null && selectedItem.getLabel().length() > 0) {
				actualLine.set(idxEditor, selectedItem.getLabel());
			}
		}
		boolean focused = false;
		if (! isLast) {
			for (int j = idxEditor + 1; j < m_FocusEditors.size(); j++) {
				// - set focus to next editor
				if (m_FocusEditors.get(j) instanceof InputElement) {
					if (((InputElement)m_FocusEditors.get(j)).isReadonly()) {
						// if this field is readonly then skip to next (i.e. lot can be readonly)
						continue;
					}
				}
				m_FocusEditors.get(j).setFocus(true);
				focused = true;
				break;
			}
		}
			
		if (!isMandatoryFilled())
			return;
		
		if (! focused || fromAllButton) {
			// - on last field save and move to new record
			saveAndNewLine(fromAllButton);
		}
		else
		{
			if (!multipleASIData.isEmpty())
			{
				boolean containData = false;
				for (Object value : actualLine)
				{
					if (value instanceof String)
					{
						if (!Util.isEmpty((String) value))
						{
							containData = true;
							break;
						}
					}
					else if (value != null)
					{
						containData = true;
						break;
					}
				}
				
				bNext.setEnabled(containData);
				confirmPanel.getButton("Ok").setEnabled(containData && multipleASIData.size() >= m_QtyRequired.intValue());
			}
		}
	}

	private void cmd_next() {
		saveAndNewLine(false);
	}

	private void saveAndNewLine(boolean fromAllButton) {
		if (saveSelection()) {
			if (m_QtyRequired.signum() != 0 && multipleASIData.size() >= m_QtyRequired.abs().intValue()) {
				bNext.setEnabled(true);
				confirmPanel.getButton("Ok").setEnabled(true);
				bSerNo.setEnabled(false);
				bAllSerNo.setEnabled(false);
				
				statusBar.setValue(Msg.getMsg(m_ctx, "Quantity") + " = " + Math.min(Math.max(1, multipleASIData.size()), m_QtyRequired.intValue()) + " / " + m_QtyRequired);
			} else {
				if (!multipleASIData.isEmpty())
				{
					Vector<Object> actualLine = multipleASIData.get(multipleASIData.size()-1);
					boolean containData = false;
					for (Object value : actualLine)
					{
						if (value instanceof String)
						{
							if (!Util.isEmpty((String) value))
							{
								containData = true;
								break;
							}
						}
						else if (value != null)
						{
							containData = true;
							break;
						}
					}
					
					bNext.setEnabled(containData);
					confirmPanel.getButton("Ok").setEnabled(containData && multipleASIData.size() >= m_QtyRequired.abs().intValue());
				}
				
				long currentTime = System.currentTimeMillis();
				// if trying to save a new record within is because the event was triggered twice
				// possibly onchange event and next button
				if (fromAllButton || currentTime - m_lastSavedTime >= 300) {
					//	Get Model
					m_masi = MAttributeSetInstance.get(m_ctx, 0, m_M_Product_ID);
					m_ASIs.add(m_masi);
					m_newASIs.add(m_masi);
					MAttributeSet as = m_masi.getMAttributeSet();
					Vector<Object> line = new Vector<Object>();
					if (!m_productWindow && as.isSerNo())
					{
						fieldSerNo.setText(m_masi.getSerNo());
						line.add(m_masi.getSerNo());
					}
					if (!m_productWindow && as.isLot()) {
						fieldLotString.setText (m_masi.getLot());
						line.add(m_masi.getLot());
					}
					if (!m_productWindow && as.isGuaranteeDate())
					{
						fieldGuaranteeDate.setValue(m_masi.getGuaranteeDate(true));
						line.add(m_masi.getGuaranteeDate());
					}
					if (!m_productWindow && activityEditor != null)
					{
						activityEditor.setValue(m_masi.get_Value(I_C_Activity.COLUMNNAME_C_Activity_ID));
						line.add(activityEditor.getDisplay());
					}
					for (int i = 0; i < m_FocusEditors.size(); i++) {
						HtmlBasedComponent lineEditor = m_FocusEditors.get(i);
						if (lineEditor instanceof Decimalbox) {
							((Decimalbox)lineEditor).setValue(Env.ZERO);
							line.add(((Decimalbox)lineEditor).getValue());
						} else if (lineEditor instanceof Listbox) {
							((Listbox)lineEditor).setSelectedIndex(0);							
							ListItem item = ((Listbox)lineEditor).getSelectedItem();
							line.add("");
							line.set(i, item != null ? item.getValue() : "");
						} else if (lineEditor instanceof Textbox) {
							((Textbox)lineEditor).setText(null);
							line.add("");
						}
					}
					
					fieldDescription.setText("");
					
					multipleASIData.add(line);
					m_FocusEditors.get(0).setFocus(true);
					m_lastSavedTime = System.currentTimeMillis();
					
					if (multipleASIData.size() >= m_QtyRequired.abs().intValue())
					{
						bNext.setEnabled(false);
						confirmPanel.getButton("Ok").setEnabled(false);
					}
					statusBar.setValue(Msg.getMsg(m_ctx, "Quantity") + " = " + Math.min(Math.max(1, multipleASIData.size()), m_QtyRequired.intValue()) + " / " + m_QtyRequired);					
				}
			}
		}
	}

	private boolean saveDocumentLines() {
		String tableName = m_gridTab.getTableName();
		BigDecimal one;
		if (m_QtyRequired.signum() < 0)
			one = Env.ONE.negate();
		else
			one = Env.ONE;
		m_gridTab.setValue(m_columnName, m_ASIs.get(0).getM_AttributeSetInstance_ID());
		//Modification to use QtyEntered instead the native qty column of the table
		/*if (   MInOutLine.Table_Name.equals(tableName)
			|| MMovementLine.Table_Name.equals(tableName)) {
			m_gridTab.setValue("MovementQty", one);
		} else if (MInventoryLine.Table_Name.equals(tableName)) {
			m_gridTab.setValue("QtyInternalUse", one); // just for internal use, not cost adjustment or physical inventory
		} else if (MProductionLine.Table_Name.equals(tableName)) {
			Object isEndProduct = m_gridTab.getValue("IsEndProduct");
			if (isEndProduct instanceof Boolean && (Boolean) isEndProduct) {
				m_gridTab.setValue("MovementQty", one);
			} else {
				m_gridTab.setValue("QtyUsed", one);
			}
			m_gridTab.setValue("PlannedQty", one);
		}*/
		if (   MInOutLine.Table_Name.equals(tableName)
				|| MMovementLine.Table_Name.equals(tableName)) {
			if(m_gridTab.getValue("QtyEntered")!=null)
				m_gridTab.setValue("QtyEntered", one);
			else
				m_gridTab.setValue("MovementQty", one);
			} else if (MInventoryLine.Table_Name.equals(tableName)) {
				if(m_gridTab.getValue("QtyEntered")!=null)
					m_gridTab.setValue("QtyEntered", one);
				else
					m_gridTab.setValue("QtyInternalUse", one); // just for internal use, not cost adjustment or physical inventory
			} else if (MProductionLine.Table_Name.equals(tableName)) {
				Object isEndProduct = m_gridTab.getValue("IsEndProduct");
				if(m_gridTab.getValue("QtyEntered")!=null)
					m_gridTab.setValue("QtyEntered", one);
				else
					if (isEndProduct instanceof Boolean && (Boolean) isEndProduct) {
						m_gridTab.setValue("MovementQty", one);
					} else {
						m_gridTab.setValue("QtyUsed", one);
					}
				m_gridTab.setValue("PlannedQty", one);
			}		
			if (MInOutLine.Table_Name.equals(tableName)) {
				m_gridTab.setValue("QtyEntered", one);
				MProduct product = MProduct.get(m_ctx, m_M_Product_ID);
				if (product != null) {
					//Modification to not reset uom to the product main unit
					if(product.getC_UOM_ID()==(Integer)m_gridTab.getValue("C_UOM_ID"))
						m_gridTab.setValue("C_UOM_ID", product.getC_UOM_ID());
				}				
			}
		m_gridTab.dataSave(true);

		PO po = null;
		if (MInOutLine.Table_Name.equals(tableName)) {
			po = new MInOutLine(m_ctx, m_gridTab.getRecord_ID(), null);
		} else if (MMovementLine.Table_Name.equals(tableName)) {
			po = new MMovementLine(m_ctx, m_gridTab.getRecord_ID(), null);
		} else if (MProductionLine.Table_Name.equals(tableName)) {
			po = new MProductionLine(m_ctx, m_gridTab.getRecord_ID(), null);
		} else if (MInventoryLine.Table_Name.equals(tableName)) {
			po = new MInventoryLine(m_ctx, m_gridTab.getRecord_ID(), null);
		}
		int line = po.get_ValueAsInt("Line");
		for (int i = 1; i < m_ASIs.size(); i++) {
			if (m_ASIs.get(i).getM_AttributeSetInstance_ID() != 0) {
				PO newPO = null;
				if (MInOutLine.Table_Name.equals(tableName)) {
					newPO = new MInOutLine(m_ctx, 0, null);
				} else if (MMovementLine.Table_Name.equals(tableName)) {
					newPO = new MMovementLine(m_ctx, 0, null);
				} else if (MProductionLine.Table_Name.equals(tableName)) {
					newPO = new MProductionLine(m_ctx, 0, null);
				} else if (MInventoryLine.Table_Name.equals(tableName)) {
					newPO = new MInventoryLine(m_ctx, 0, null);
				}
				PO.copyValues(po, newPO);
				line += 10;
				newPO.set_ValueOfColumn("Line", line);
				newPO.setAD_Org_ID(po.getAD_Org_ID());
				newPO.set_ValueOfColumn("M_AttributeSetInstance_ID", m_ASIs.get(i).getM_AttributeSetInstance_ID());
				if (MMovementLine.Table_Name.equals(tableName)) { // Ticket 1006562
					newPO.set_ValueOfColumn("M_AttributeSetInstanceTo_ID", m_ASIs.get(i).getM_AttributeSetInstance_ID());
				}
				newPO.saveEx();
			}
		}
		return true;
	}

	/**
	 * 	Instance Selection Button
	 * 	@return true if selected
	 */
	private void cmd_select()
	{
		log.config("");
		
		int M_Warehouse_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, "M_Warehouse_ID", true);
		
		int C_DocType_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, "C_DocType_ID");
		if (C_DocType_ID > 0) {
			MDocType doctype = new MDocType (m_ctx, C_DocType_ID, null);
			String docbase = doctype.getDocBaseType();
			if (docbase.equals(MDocType.DOCBASETYPE_MaterialReceipt))
				M_Warehouse_ID = 0;
		}
		
		// teo_sarca [ 1564520 ] Inventory Move: can't select existing attributes
		int M_Locator_ID = 0;
//		if (m_AD_Column_ID == COLUMN_M_MOVEMENTLINE_M_ATTRIBUTESETINSTANCE_ID) { // TODO: hardcoded: M_MovementLine[324].M_AttributeSetInstance_ID[8551]
//			M_Locator_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, X_M_MovementLine.COLUMNNAME_M_Locator_ID, true); // only window
//		}
		if (m_columnName.equals("M_AttributeSetInstance_ID"))
		{
			M_Locator_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, m_gridTab.getTabNo(), "M_Locator_ID");
			if (M_Locator_ID == 0)
				M_Locator_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, "M_Locator_ID", true);
		}
		else if (m_columnName.equals("M_AttributeSetInstanceTo_ID"))
		{
			M_Locator_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, m_gridTab.getTabNo(), "M_LocatorTo_ID");
			if (M_Locator_ID == 0)
				M_Locator_ID = Env.getContextAsInt(m_ctx, m_WindowNoParent, "M_LocatorTo_ID", true);
		}
		
		String title = "";
		//	Get Text
		String sql = "SELECT p.Name, w.Name, w.M_Warehouse_ID FROM M_Product p, M_Warehouse w "
			+ "WHERE p.M_Product_ID=? AND w.M_Warehouse_ID"
				+ (M_Locator_ID <= 0 ? "=?" : " IN (SELECT M_Warehouse_ID FROM M_Locator where M_Locator_ID=?)"); // teo_sarca [ 1564520 ]
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, m_M_Product_ID);
			pstmt.setInt(2, M_Locator_ID <= 0 ? M_Warehouse_ID : M_Locator_ID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				title = rs.getString(1) + " - " + rs.getString(2);
//				M_Warehouse_ID = rs.getInt(3); // fetch the actual warehouse - teo_sarca [ 1564520 ]
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//		
		final WPAttributeInstance pai = new WPAttributeInstance(title, 
			M_Warehouse_ID, M_Locator_ID, m_M_Product_ID, m_C_BPartner_ID);
		pai.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (pai.getM_AttributeSetInstance_ID() != -1)
				{
					m_M_AttributeSetInstance_ID = pai.getM_AttributeSetInstance_ID();
					m_M_Locator_ID = pai.getM_Locator_ID();
					m_changed = true;
					loadExistingASI();
				}				
			}
		});		
	}	//	cmd_select

	protected void loadExistingASI() {
		// load the data of existing ASI in the current line
		m_ASIs.remove(m_ASIs.size()-1);
		multipleASIData.remove(multipleASIData.size()-1);
		Vector<Object> line = new Vector<Object>();

		MAttributeSet as = null;
		
		//	Get Model
		m_masi = MAttributeSetInstance.get(m_ctx, m_M_AttributeSetInstance_ID, m_M_Product_ID);
		if (m_masi == null)
		{
			throw new AdempiereException("No Model for M_AttributeSetInstance_ID=" + m_M_AttributeSetInstance_ID + ", M_Product_ID=" + m_M_Product_ID);
		}
		Env.setContext(m_ctx, m_WindowNo, "M_AttributeSet_ID", m_masi.getM_AttributeSet_ID());

		//	Get Attribute Set
		as = m_masi.getMAttributeSet();
		m_ASIs.add(m_masi);
		
		//	SerNo
		if (!m_productWindow && as.isSerNo())
		{
			fieldSerNo.setText(m_masi.getSerNo());
			line.add(m_masi.getSerNo());
		}	//	SerNo

		//	Lot
		if (!m_productWindow && as.isLot())
		{
			fieldLotString.setText (m_masi.getLot());
			line.add(m_masi.getLot());
		}	//	Lot

		//	GuaranteeDate
		if (!m_productWindow && as.isGuaranteeDate())
		{
			fieldGuaranteeDate.setValue(m_masi.getGuaranteeDate());
			line.add(m_masi.getGuaranteeDate());
		}	//	GuaranteeDate

		// activity
		if (!m_productWindow && activityEditor != null)
		{
			activityEditor.setValue(m_masi.get_Value(I_C_Activity.COLUMNNAME_C_Activity_ID));
			line.addElement(activityEditor.getDisplay());
		}
		
		//	Show Product Attributes
		if (! m_productWindow)
		{
			//	All Attributes
			MAttribute[] attributes = as.getMAttributes (true);
			if (log.isLoggable(Level.FINE)) log.fine ("Instance Attributes=" + attributes.length);
			for (int i = 0; i < attributes.length; i++) {
				MAttribute attribute = attributes[i];
				if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
				{
					Listbox editor = (Listbox) m_editors.get(i);
					setListAttribute(attribute, editor, line);
				}
				else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attribute.getAttributeValueType()))
				{
					NumberBox editor = (NumberBox) m_editors.get(i);
					setNumberAttribute(attribute, editor, line);
				}
				else	//	Text Field
				{
					Textbox editor = (Textbox) m_editors.get(i);
					setStringAttribute(attribute, editor, line);
				}
			}
		}

		//	Attribute Set Instance Description
		fieldDescription.setText(m_masi.getDescription());

		//  Set Model
		multipleASIData.add(line);
		multipleASITable.clearTable();
		ListModelTable model = new ListModelTable(multipleASIData);
		multipleASITable.setData(model, multipleASIColumnNames);
		saveAndNewLine(false);
	}

	/**
	 * 	Zoom M_Lot
	 */
	private void cmd_zoom()
	{
		int M_Lot_ID = 0;
		ListItem pp = fieldLot.getSelectedItem();
		if (pp != null)
			M_Lot_ID = (Integer) pp.getValue();
		MQuery zoomQuery = new MQuery("M_Lot");
		zoomQuery.addRestriction("M_Lot_ID", MQuery.EQUAL, M_Lot_ID);
		log.info(zoomQuery.toString());
	}	//	cmd_zoom

	private boolean isMandatoryFilled()
	{
		MAttributeSet as = m_masi.getMAttributeSet();
		if (as == null)
			return true;
		
		if (!m_productWindow && as.isLot())
		{
			String text = fieldLotString.getText();
			if (as.isLotMandatory() && (text == null || text.length() == 0))
				return false;
		}	//	Lot
		if (!m_productWindow && as.isSerNo())
		{
			String text = fieldSerNo.getText();
			if (as.isSerNoMandatory() && (text == null || text.length() == 0))
				return false;
		}	//	SerNo
		if (!m_productWindow && as.isGuaranteeDate())
		{
			Date gDate = fieldGuaranteeDate.getValue();
			Timestamp ts = gDate != null ? new Timestamp(gDate.getTime()) : null;
			if (as.isGuaranteeDateMandatory() && ts == null)
				return false;
		}	//	GuaranteeDate
		return true;
	}
	
	/**
	 *	Save Selection
	 *	@return true if saved
	 */
	private boolean saveSelection()
	{
		log.info("");
		MAttributeSet as = m_masi.getMAttributeSet();
		if (as == null)
			return true;
		//
		String mandatory = "";
		if (!m_productWindow && as.isLot())
		{
			if (log.isLoggable(Level.FINE)) log.fine("Lot=" + fieldLotString.getText ());
			String text = fieldLotString.getText();
			m_masi.setLot (text);
			if (as.isLotMandatory() && (text == null || text.length() == 0))
				mandatory += " - " + Msg.translate(m_ctx, "Lot");
			m_changed = true;
		}	//	Lot
		if (!m_productWindow && as.isSerNo())
		{
			if (log.isLoggable(Level.FINE)) log.fine("SerNo=" + fieldSerNo.getText());
			String text = fieldSerNo.getText();
			m_masi.setSerNo(text);
			if (as.isSerNoMandatory() && (text == null || text.length() == 0))
				mandatory += " - " + Msg.translate(m_ctx, "SerNo");
			m_changed = true;
		}	//	SerNo
		if (!m_productWindow && as.isGuaranteeDate())
		{
			if (log.isLoggable(Level.FINE)) log.fine("GuaranteeDate=" + fieldGuaranteeDate.getValue());
			Date gDate = fieldGuaranteeDate.getValue();
			Timestamp ts = gDate != null ? new Timestamp(gDate.getTime()) : null;
			m_masi.setGuaranteeDate(ts);
			if (as.isGuaranteeDateMandatory() && ts == null)
				mandatory += " - " + Msg.translate(m_ctx, "GuaranteeDate");
			m_changed = true;
		}	//	GuaranteeDate
		if (!m_productWindow && activityEditor != null) {
			Object value = activityEditor.getValue();
			int newActivityId = 0;
			if (value != null && value instanceof Number) {
				newActivityId = ((Number)value).intValue();
			}
			int C_Activity_ID = m_masi.get_ValueAsInt("C_Activity_ID");
			if (C_Activity_ID != newActivityId) {
				m_masi.set_ValueOfColumn("C_Activity_ID", newActivityId > 0 ? newActivityId : null);
				m_changed = true;
			}
		} //activity
		
		//	***	Save Attributes ***
		//	New Instance
		if (m_changed || m_masi.getM_AttributeSetInstance_ID() == 0)
		{
			m_masi.saveEx ();
			m_M_AttributeSetInstance_ID = m_masi.getM_AttributeSetInstance_ID ();
		}

		//	Save Instance Attributes
		MAttribute[] attributes = as.getMAttributes(!m_productWindow);
		for (int i = 0; i < attributes.length; i++)
		{
			if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attributes[i].getAttributeValueType()))
			{
				Listbox editor = (Listbox)m_editors.get(i);
				ListItem item = editor.getSelectedItem();
				MAttributeValue value = item != null ? (MAttributeValue)item.getValue() : null;
				if (log.isLoggable(Level.FINE)) log.fine(attributes[i].getName() + "=" + value);
				if (attributes[i].isMandatory() && value == null)
					mandatory += " - " + attributes[i].getName();
				attributes[i].setMAttributeInstance(m_M_AttributeSetInstance_ID, value);
			}
			else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attributes[i].getAttributeValueType()))
			{
				NumberBox editor = (NumberBox)m_editors.get(i);
				BigDecimal value = editor.getValue();
				if (log.isLoggable(Level.FINE)) log.fine(attributes[i].getName() + "=" + value);
				if (attributes[i].isMandatory() && value == null)
					mandatory += " - " + attributes[i].getName();
				//setMAttributeInstance doesn't work without decimal point
				if (value != null && value.scale() == 0)
					value = value.setScale(1, RoundingMode.HALF_UP);
				attributes[i].setMAttributeInstance(m_M_AttributeSetInstance_ID, value);
			}
			else
			{
				Textbox editor = (Textbox)m_editors.get(i);
				String value = editor.getText();
				if (log.isLoggable(Level.FINE)) log.fine(attributes[i].getName() + "=" + value);
				if (attributes[i].isMandatory() && (value == null || value.length() == 0))
					mandatory += " - " + attributes[i].getName();
				attributes[i].setMAttributeInstance(m_M_AttributeSetInstance_ID, value);
			}
			m_changed = true;
		}	//	for all attributes
		
		//	Save Model
		if (m_changed)
		{
			m_masi.setDescription ();
			m_masi.saveEx ();
		}
		m_M_AttributeSetInstance_ID = m_masi.getM_AttributeSetInstance_ID ();
		//
		if (mandatory.length() > 0)
		{
			Dialog.error(m_WindowNo, "FillMandatory", mandatory);
			return false;
		}
		return true;
	}	//	saveSelection

	
	/**************************************************************************
	 * 	Get Instance ID
	 * 	@return Instance ID
	 */
	public int getM_AttributeSetInstance_ID()
	{
		return m_ASIs.get(0).getM_AttributeSetInstance_ID();
	}	//	getM_AttributeSetInstance_ID

	/**
	 * 	Get Instance Name
	 * 	@return Instance Name
	 */
	public String getM_AttributeSetInstanceName()
	{
		return m_ASIs.get(0).getDescription();
	}	//	getM_AttributeSetInstanceName
	
	/**
	 * Get Locator ID
	 * @return M_Locator_ID
	 */
	public int getM_Locator_ID()
	{
		return m_M_Locator_ID; 
	}

	/**
	 * 	Value Changed
	 *	@return true if changed
	 */
	public boolean isChanged()
	{
		return m_changed;
	}	//	isChanged

	private boolean hasActivityColumn() 
	{
		MTable table = MTable.get(m_ctx, I_M_AttributeSetInstance.Table_ID);
		MColumn column = table.getColumn(I_C_Activity.COLUMNNAME_C_Activity_ID);
		return column != null && column.isActive();
	}
} //	WPAttributeMultipleDialog
