/*
 * JPanel.java
 *
 * Created on June 10, 2004, 11:28 AM
 */

package com.metavize.gui.widgets.editTable;

import com.metavize.gui.widgets.coloredTable.*;
import com.metavize.gui.widgets.dialogs.RefreshLogFailureDialog;
import com.metavize.gui.transform.*;
import com.metavize.gui.util.*;

import com.metavize.mvvm.tran.Transform;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.*;
import java.awt.*;

/**
 *
 * @author  inieves
 */
public class MLogTableJPanel extends javax.swing.JPanel {
    
    protected Transform logTransform;

    private static final long STREAM_SLEEP_MILLIS = 7500l;
    
    private static final Color TABLE_BACKGROUND_COLOR = new Color(213, 213, 226);
    
    public MLogTableJPanel(Transform logTransform) {
        this.logTransform = logTransform;

        // INIT GUI & CUSTOM INIT
        initComponents();
        entryJScrollPane.getViewport().setOpaque(true);
        entryJScrollPane.getViewport().setBackground(TABLE_BACKGROUND_COLOR);
        entryJScrollPane.setViewportBorder(new MatteBorder(2, 2, 2, 1, TABLE_BACKGROUND_COLOR));
        this.setOpaque(false);
        contentJPanel.setOpaque(false);
        Dictionary dictionary = depthJSlider.getLabelTable();
        Enumeration enumeration = dictionary.elements();
        while(enumeration.hasMoreElements()){
            Object object = enumeration.nextElement();
            if(object instanceof JComponent) ((JComponent)object).setFont(new Font("Dialog", 0, 9));
        }
        depthJSlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        
        // BUILD TABLE MODEL
        setTableModel(new LogTableModel());

    }

    
    
    public void setTableModel(MSortedTableModel mSortedTableModel){
        entryJTable.setModel( mSortedTableModel );
        entryJTable.setColumnModel( mSortedTableModel.getTableColumnModel() );
        mSortedTableModel.setTableHeader( entryJTable.getTableHeader() );
        mSortedTableModel.hideColumns( entryJTable );
    }
    
    public MSortedTableModel getTableModel(){
        return (MSortedTableModel) entryJTable.getModel();
    }
    

    public MColoredJTable getJTable(){
        return (MColoredJTable) entryJTable;
    }
        
    protected Vector generateRows(Object settings) {
        return null;
    }

    
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        contentJPanel = new javax.swing.JPanel();
        tableJPanel = new javax.swing.JPanel();
        entryJScrollPane = new javax.swing.JScrollPane();
        entryJTable = new MColoredJTable();
        depthJSlider = new javax.swing.JSlider();
        eventJPanel = new javax.swing.JPanel();
        refreshLogJButton = new javax.swing.JButton();
        streamingJToggleButton = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridBagLayout());

        contentJPanel.setLayout(new java.awt.GridBagLayout());

        tableJPanel.setLayout(new java.awt.GridBagLayout());

        tableJPanel.setMinimumSize(new java.awt.Dimension(40, 40));
        entryJScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        entryJScrollPane.setDoubleBuffered(true);
        entryJTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        entryJTable.setDoubleBuffered(true);
        entryJScrollPane.setViewportView(entryJTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tableJPanel.add(entryJScrollPane, gridBagConstraints);

        depthJSlider.setMajorTickSpacing(100);
        depthJSlider.setMaximum(1000);
        depthJSlider.setMinimum(100);
        depthJSlider.setMinorTickSpacing(50);
        depthJSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        depthJSlider.setPaintLabels(true);
        depthJSlider.setPaintTicks(true);
        depthJSlider.setSnapToTicks(true);
        depthJSlider.setToolTipText("<html>\n<b>Log Depth Slider</b><br>\nThis slider allows you to specify how many events are visible at a time<br>\nwhen the \"Refresh Log\" or \"Start Streaming\" buttons are pressed.</html>");
        depthJSlider.setMaximumSize(new java.awt.Dimension(65, 32767));
        depthJSlider.setMinimumSize(new java.awt.Dimension(65, 36));
        depthJSlider.setPreferredSize(new java.awt.Dimension(65, 200));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 15, 10);
        tableJPanel.add(depthJSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        contentJPanel.add(tableJPanel, gridBagConstraints);

        eventJPanel.setLayout(new java.awt.GridBagLayout());

        eventJPanel.setFocusCycleRoot(true);
        eventJPanel.setFocusable(false);
        eventJPanel.setOpaque(false);
        refreshLogJButton.setFont(new java.awt.Font("Dialog", 0, 12));
        refreshLogJButton.setText("<html><b>Refresh</b> Log</html>");
        refreshLogJButton.setDoubleBuffered(true);
        refreshLogJButton.setFocusPainted(false);
        refreshLogJButton.setFocusable(false);
        refreshLogJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshLogJButton.setMargin(new java.awt.Insets(3, 5, 3, 5));
        refreshLogJButton.setMaximumSize(new java.awt.Dimension(100, 27));
        refreshLogJButton.setMinimumSize(new java.awt.Dimension(100, 27));
        refreshLogJButton.setPreferredSize(new java.awt.Dimension(100, 27));
        refreshLogJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshLogJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        eventJPanel.add(refreshLogJButton, gridBagConstraints);

        streamingJToggleButton.setFont(new java.awt.Font("Dialog", 0, 12));
        streamingJToggleButton.setText("<html> <b>Start</b> Streaming </html>");
        streamingJToggleButton.setFocusPainted(false);
        streamingJToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        streamingJToggleButton.setMinimumSize(new java.awt.Dimension(125, 27));
        streamingJToggleButton.setPreferredSize(new java.awt.Dimension(125, 27));
        streamingJToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                streamingJToggleButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        eventJPanel.add(streamingJToggleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        contentJPanel.add(eventJPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 2);
        add(contentJPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private StreamThread streamThread;
    
    private void streamingJToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streamingJToggleButtonActionPerformed
       if(streamingJToggleButton.isSelected()){
           refreshLogJButton.setEnabled(false); 
           streamThread = new StreamThread();
           streamingJToggleButton.setText("<html><center><b>Stop</b> Streaming</center></html>");
       }
       else{
           refreshLogJButton.setEnabled(true);
           streamThread.interrupt();
           streamingJToggleButton.setText("<html><center><b>Start</b> Streaming</center></html>");
       }
    }//GEN-LAST:event_streamingJToggleButtonActionPerformed

    private void refreshLogJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshLogJButtonActionPerformed
	try{
	    getTableModel().doRefresh(null);
	}
	catch(Exception e){
	    try{
		Util.handleExceptionWithRestart("Error refreshing log", e);
	    }
	    catch(Exception f){
		Util.handleExceptionNoRestart("Error refreshing log", f);
		new RefreshLogFailureDialog( logTransform.getTransformDesc().getDisplayName() );
	    }
	}
    }//GEN-LAST:event_refreshLogJButtonActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentJPanel;
    protected javax.swing.JSlider depthJSlider;
    protected javax.swing.JScrollPane entryJScrollPane;
    protected javax.swing.JTable entryJTable;
    private javax.swing.JPanel eventJPanel;
    private javax.swing.JButton refreshLogJButton;
    private javax.swing.JToggleButton streamingJToggleButton;
    private javax.swing.JPanel tableJPanel;
    // End of variables declaration//GEN-END:variables
    

class StreamThread extends Thread {
    public StreamThread(){
        this.start();
    }
    
    public void run(){
        try{
            while(true){
                getTableModel().doRefresh(null);
                this.sleep(STREAM_SLEEP_MILLIS);
            }
        }
        catch(InterruptedException e){
            // this is normal, means the thread was stopped by a button press
        }
        catch(Exception f){
            try{
                Util.handleExceptionWithRestart("Error streaming event log", f);
            }
            catch(Exception g){
                Util.handleExceptionNoRestart("Error streaming event log", g);
            }
        }
    }
}
    
    
class LogTableModel extends MSortedTableModel{
    
    public TableColumnModel getTableColumnModel(){

        DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
        //                                 #   min  rsz    edit   remv   desc   typ               def
	addTableColumn( tableColumnModel,  0,  125, true,  false, false, false, String.class, null, "timestamp" );
        addTableColumn( tableColumnModel,  1,  55,  true,  false, false, false, String.class, null, "action" );
        addTableColumn( tableColumnModel,  2,  100, true,  false, false, false, String.class, null, "traffic" );
        addTableColumn( tableColumnModel,  3,  100, true,  false, false, false, String.class, null, "reason" );
        addTableColumn( tableColumnModel,  4,  100, true,  false, false, false, String.class, null, "direction" );
        addTableColumn( tableColumnModel,  5,  155, true,  false, false, false, String.class, null, "server" );
        addTableColumn( tableColumnModel,  6,  155, true,  false, false, false, String.class, null, "client" );
        return tableColumnModel;
    }
    
    public void generateSettings(Object settings, boolean validateOnly) throws Exception {
    }
        
    public Vector generateRows(Object settings) {
        return MLogTableJPanel.this.generateRows(null);
    }

}
}
