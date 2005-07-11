/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */
package com.metavize.tran.protofilter.gui;

import com.metavize.mvvm.tran.Transform;
import com.metavize.gui.transform.*;
import com.metavize.gui.pipeline.MPipelineJPanel;
import com.metavize.mvvm.tran.TransformContext;
import com.metavize.gui.widgets.editTable.*;
import com.metavize.gui.util.*;

import com.metavize.tran.protofilter.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class ProtoConfigJPanel extends MEditTableJPanel{

    public ProtoConfigJPanel() {
        super(true, true);
        super.setFillJButtonEnabled( false );
        super.setInsets(new Insets(4, 4, 2, 2));
        super.setTableTitle("protocols");
        super.setDetailsTitle("protocol details");
        super.setAddRemoveEnabled(true);
        
        // create actual table model
        ProtoTableModel protoTableModel = new ProtoTableModel();
        this.setTableModel( protoTableModel );
	protoTableModel.setSortingStatus(2, ProtoTableModel.ASCENDING);
    }
}


class ProtoTableModel extends MSortedTableModel{ 

    private static final int T_TW = Util.TABLE_TOTAL_WIDTH;
    private static final int C0_MW = Util.STATUS_MIN_WIDTH; /* status */
    private static final int C1_MW = Util.LINENO_MIN_WIDTH; /* # */
    private static final int C2_MW = 140; /* category */
    private static final int C3_MW = 100; /* protocol */
    private static final int C4_MW = 55;  /* block */
    private static final int C5_MW = 55;  /* log */
    private static final int C6_MW = 100; /* description */
    private static final int C7_MW = Util.chooseMax(T_TW - (C0_MW + C1_MW + C2_MW + C3_MW + C4_MW + C5_MW + C6_MW), 120); /* signature */

    
    public TableColumnModel getTableColumnModel(){
        
	ProtoFilterPattern tempPattern = new ProtoFilterPattern();

        DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
        //                                 #  min    rsz    edit   remv   desc   typ            def
        addTableColumn( tableColumnModel,  0, C0_MW, false, false, false, false, String.class,  null, sc.TITLE_STATUS );
        addTableColumn( tableColumnModel,  1, C1_MW, false, false, true,  false, Integer.class, null, sc.TITLE_INDEX );
        addTableColumn( tableColumnModel,  2, C2_MW, true,  true,  false, false, String.class,  sc.EMPTY_CATEGORY, sc.TITLE_CATEGORY );
        addTableColumn( tableColumnModel,  3, C3_MW, true,  true,  false, false, String.class,  sc.empty( "no protocol" ), "protocol");
        addTableColumn( tableColumnModel,  4, C4_MW, false, true,  false, false, Boolean.class, "false", sc.bold("block"));
        addTableColumn( tableColumnModel,  5, C5_MW, false, true,  false, false, Boolean.class, "false", sc.bold("log"));
        addTableColumn( tableColumnModel,  6, C6_MW, true,  true,  false, true,  String.class,  sc.EMPTY_DESCRIPTION, sc.TITLE_DESCRIPTION );
        addTableColumn( tableColumnModel,  7, C7_MW, true,  true,  false, false, String.class,  sc.empty("no signature"), "signature");
        addTableColumn( tableColumnModel,  8, 0, false, false, true,  false, String.class,  tempPattern.getQuality(), "quality");
        return tableColumnModel;
    }
    
    public void generateSettings(Object settings, boolean validateOnly) throws Exception{
        List elemList = new ArrayList();
	for( Vector rowVector : (Vector<Vector>) this.getDataVector() ){
            
	    ProtoFilterPattern newElem = new ProtoFilterPattern();
            newElem.setCategory( (String) rowVector.elementAt(2) );
            newElem.setProtocol( (String) rowVector.elementAt(3) );
            newElem.setBlocked( ((Boolean) rowVector.elementAt(4)).booleanValue());
            newElem.setLog(((Boolean) rowVector.elementAt(5)).booleanValue());
            newElem.setDescription( (String) rowVector.elementAt(6) );
            newElem.setDefinition( (String) rowVector.elementAt(7) );
	    newElem.setQuality( (String) rowVector.elementAt(8) );

            elemList.add(newElem);
        }

	// SAVE SETTINGS ////////
	if( !validateOnly ){
	    ProtoFilterSettings transformSettings = (ProtoFilterSettings) settings;
	    transformSettings.setPatterns(elemList);
	}

    }
    
    public Vector generateRows(Object settings){
	ProtoFilterSettings protoFilterSettings = (ProtoFilterSettings) settings;
        Vector allRows = new Vector();
        int count = 1;
	for( ProtoFilterPattern newElem : (List<ProtoFilterPattern>) protoFilterSettings.getPatterns() ){

            Vector row = new Vector();
            row.add(super.ROW_SAVED);
            row.add(new Integer(count));
            row.add(newElem.getCategory());
            row.add(newElem.getProtocol());
            row.add(Boolean.valueOf(newElem.isBlocked()));
            row.add(Boolean.valueOf(newElem.getLog()));
            row.add(newElem.getDescription());
            row.add(newElem.getDefinition());
	    row.add(newElem.getQuality());

            allRows.add(row);
	    count++;
        }
        return allRows;
    }
}
