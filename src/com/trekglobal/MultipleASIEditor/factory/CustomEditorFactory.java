/******************************************************************************
 * Product: TrekCloud ERP                                                     *
 * Copyright (C) 2016 Trek Global All Rights Reserved.                        *
 * Copyright (C) 2016 Carlos Ruiz                                             *
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

package com.trekglobal.MultipleASIEditor.factory;

import org.adempiere.webui.editor.IEditorConfiguration;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.factory.IEditorFactory;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MMovementLine;
import org.compiere.model.MProductionLine;
import org.compiere.model.MSysConfig;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

import com.trekglobal.MultipleASIEditor.editor.WPAttributeMultipleEditor;

/**
 * @author Carlos Ruiz
 */
public class CustomEditorFactory implements IEditorFactory {

	/**
	 * 
	 */
	public CustomEditorFactory() {
	}

	@Override
	public WEditor getEditor(GridTab gridTab, GridField gridField,
			boolean tableEditor) {
		return getEditor(gridTab, gridField, tableEditor, null);
	}
	
	@Override
	public WEditor getEditor(GridTab gridTab, GridField gridField,
			boolean tableEditor, IEditorConfiguration editorConfiguration) {

		if (   gridField.getDisplayType() == DisplayType.PAttribute
			&& gridTab != null
			&& MSysConfig.getBooleanValue("ENABLE_MULTI_ASI_EDITOR", true, Env.getAD_Client_ID(Env.getCtx()))) {

			String tableName = gridTab.getTableName();
			if (   MInOutLine.Table_Name.equals(tableName)
				|| MInventoryLine.Table_Name.equals(tableName)
				|| MMovementLine.Table_Name.equals(tableName)
				|| MProductionLine.Table_Name.equals(tableName)
				) {
				WEditor editor = new WPAttributeMultipleEditor(gridTab, gridField);
		        editor.setTableEditor(tableEditor);
		        return editor;

			}

		}
		
		return null;
	}

}
