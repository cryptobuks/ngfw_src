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

package com.metavize.tran.mail.gui;

import com.metavize.gui.transform.*;
import com.metavize.gui.pipeline.MPipelineJPanel;
import com.metavize.gui.widgets.editTable.*;
import com.metavize.gui.util.*;

import com.metavize.mvvm.tran.TransformContext;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.metavize.tran.mail.papi.*;

public class QuarantinableForwardsJPanel extends MEditTableJPanel{

    public QuarantinableForwardsJPanel(TransformContext transformContext) {
        super(true, true);
        super.setFillJButtonEnabled( true );
        super.setInsets(new Insets(4, 4, 2, 2));
        super.setTableTitle("");
        super.setDetailsTitle("");
        super.setAddRemoveEnabled(true);
        
        // create actual table model
        ForwardsModel forwardsModel = new ForwardsModel(transformContext);
        this.setTableModel( forwardsModel );
    }
    



class ForwardsModel extends MSortedTableModel{ 
    
    private static final int  T_TW  = Util.TABLE_TOTAL_WIDTH;
    private static final int  C0_MW = Util.STATUS_MIN_WIDTH; /* status */
    private static final int  C1_MW = Util.LINENO_MIN_WIDTH; /* # */
    private static final int  C2_MW = 150;  /* d-list address */
    private static final int  C3_MW = 150;  /* send-to address */
    private static final int  C4_MW = 120;  /* category */
    private static final int  C5_MW = 120;  /* description */
    
    private TransformContext transformContext;

    public ForwardsModel(TransformContext transformContext){
	this.transformContext = transformContext;
    }

    protected boolean getSortable(){ return false; }
    
    public TableColumnModel getTableColumnModel(){
        
        DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
        //                                 #   min    rsz    edit   remv   desc   typ            def
        addTableColumn( tableColumnModel,  0,  C0_MW, false, false, false, false, String.class,  null, sc.TITLE_STATUS );
        addTableColumn( tableColumnModel,  1,  C1_MW, false, false, false, false, Integer.class, null, sc.TITLE_INDEX );
        addTableColumn( tableColumnModel,  2,  C2_MW, true,   true,  false, false, String.class, "somedistributionlist@somewhere.com", sc.html("distribution<br>list address") );
        addTableColumn( tableColumnModel,  3,  C3_MW, true,  true,  false, false, String.class, "someone@somewhere.com", sc.html("send to<br>address") );
        addTableColumn( tableColumnModel,  4,  C4_MW, true,  true,  false, false, String.class, sc.EMPTY_CATEGORY, sc.TITLE_CATEGORY );
        addTableColumn( tableColumnModel,  5,  C5_MW, true,  true,  false, true,  String.class, sc.EMPTY_DESCRIPTION, sc.TITLE_DESCRIPTION );
        addTableColumn( tableColumnModel,  6,  10,    false, false, true,  false, EmailAddressRule.class, null, "");
        return tableColumnModel;
    }
    
    
    public void generateSettings(Object settings, Vector<Vector> tableVector, boolean validateOnly) throws Exception {
        List<EmailAddressPairRule> elemList = new ArrayList(tableVector.size());
	EmailAddressPairRule newElem = null;
        int rowIndex = 0;

        for( Vector rowVector : tableVector ){
	    rowIndex++;
	    newElem.setAddress1( (String) rowVector.elementAt(2) );
	    newElem.setAddress2( (String) rowVector.elementAt(3) );
	    newElem.setCategory( (String) rowVector.elementAt(4) );
	    newElem.setDescription( (String) rowVector.elementAt(5) );
            elemList.add(newElem);
        }
        
	// SAVE SETTINGS //////////
	if( !validateOnly ){
	    MailTransformSettings mailTransformSettings = ((MailTransform)transformContext.transform()).getMailTransformSettings();
	    mailTransformSettings.getQuarantineSettings().setAddressRemaps(elemList);
	    ((MailTransform)transformContext.transform()).setMailTransformSettings(mailTransformSettings);
	}
    }

    public Vector<Vector> generateRows(Object settings) {
	List<EmailAddressPairRule> addressList = 
            (List<EmailAddressPairRule>) ((MailTransform)transformContext.transform()).getMailTransformSettings().getQuarantineSettings().getAddressRemaps();
        Vector<Vector> allRows = new Vector<Vector>(addressList.size());
	Vector tempRow = null;
        int rowIndex = 0;

        for( EmailAddressPairRule address : addressList ){
	    rowIndex++;
	    tempRow = new Vector(7);
	    tempRow.add( super.ROW_SAVED );
	    tempRow.add( rowIndex );
            tempRow.add( address.getAddress1() );
            tempRow.add( address.getAddress2() );
            tempRow.add( address.getCategory());
	    tempRow.add( address.getDescription());
	    tempRow.add( address );
	    allRows.add( tempRow );
        }
        return allRows;
    }
    
    
}

}
