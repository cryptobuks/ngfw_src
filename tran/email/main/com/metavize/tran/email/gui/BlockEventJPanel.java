/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: BlockEventJPanel.java,v 1.5 2005/02/03 05:36:38 rbscott Exp $
 */
package com.metavize.tran.email.gui;

import com.metavize.mvvm.tran.TransformContext;
import com.metavize.mvvm.logging.*;
import com.metavize.gui.transform.*;
import com.metavize.gui.pipeline.MPipelineJPanel;
import com.metavize.gui.widgets.editTable.*;
import com.metavize.gui.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Vector;
import javax.swing.event.*;

import com.metavize.tran.email.*;

public class BlockEventJPanel extends MEditTableJPanel {
    
    
    public BlockEventJPanel(TransformContext transformContext) {

//         super();
//         super.setInsets(new Insets(4, 4, 2, 2));
//         super.setTableTitle("email rule block log");
//         super.setDetailsTitle("rule notes");
        
//         // create actual table model
//         BlockEventTableModel eventTableModel = new BlockEventTableModel(transformContext);
//         this.setTableModel( eventTableModel );

    }


}


// class BlockEventTableModel extends MSortedTableModel{ 
//   private static final StringConstants sc = StringConstants.getInstance();
    
    
//     BlockEventTableModel(TransformContext transformContext){
//         super(transformContext);
//     }
    
//     public TableColumnModel getTableColumnModel(){
        
//         DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
//         //                                 #  min  rsz    edit   remv   desc   typ            def
//         addTableColumn( tableColumnModel,  0,  30, false, false, false, false, Integer.class, null, sc.TITLE_INDEX);
//         addTableColumn( tableColumnModel,  1, 100, true,  false, false, false, String.class,  null, "time");
//         addTableColumn( tableColumnModel,  2, 150, true,  false, false, false, String.class,  null, "client");
//         addTableColumn( tableColumnModel,  3, 150, true,  false, false, false, String.class,  null, "searched for this");
//         addTableColumn( tableColumnModel,  4, 150, true,  false, false, false, String.class,  null, "<html><center>in this part<br>of the email</center></html>");
//         addTableColumn( tableColumnModel,  5,  85, true,  false, false, false, String.class,  null, "action");
//         addTableColumn( tableColumnModel,  6,  85, true,  false, false, false, String.class,  null, "subject");
//         addTableColumn( tableColumnModel,  7,  85, true,  false, false, false, String.class,  null, "from");
//         addTableColumn( tableColumnModel,  8,  85, true,  false, false, false, String.class,  null, "to");
//         return tableColumnModel;
  
//     }


//     /*
//     public Node generateTransformDescNode(Vector dataVector){
//         return null;
//     }
//     */
    
//     public void flushLog(){
//         try{
//             Util.getMvvmContext().loggingManager().clearLogs(super.transformContext.tid());
//         }
//         catch(Exception e){
//             try{
//                 Util.handleExceptionWithRestart("error resetting log", e);
//             }
//             catch(Exception f){
//                 Util.handleExceptionNoRestart("error resetting log", f);
//             }
//         }
//     }
    
//     public Vector generateRows(Object transformDescNode){
//         LogEvent newEvent;
//         CustomLogEvent tempEvent;
//         Vector allRows = new Vector();
//         Vector row;
//         int counter = 1;
//         LogEvent logEvents[] = null;
        
//         logEvents = Util.getMvvmContext().loggingManager().transformLogEvents(super.transformContext.tid());

        
//         for(int i=0; i<logEvents.length; i++){
//             newEvent = logEvents[i];
            
//             if(newEvent instanceof CustomLogEvent){
//                 tempEvent = (CustomLogEvent) newEvent;    
//                 row = new Vector(9);
                
//                 row.add(new Integer(counter));
//                 row.add(tempEvent.timestamp() );
//                 row.add(tempEvent.clientIP());
//                 row.add(tempEvent.fieldPattern());
//                 row.add(tempEvent.fieldType());
//                 row.add(tempEvent.fieldAction());
                
//                 row.add(tempEvent.subject());
//                 row.add(tempEvent.from());
//                 row.add(tempEvent.toCcBcc());
//             }
//             else{
//                 continue;
//             }
            
            
//             allRows.add(row);
//             counter++;
//         }
        
//         return allRows;
//     }   
// }
