/*
 * asdf.java
 *
 * Created on March 12, 2004, 1:46 AM
 */

package com.metavize.gui.main;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.metavize.gui.configuration.*;
import com.metavize.gui.pipeline.*;
import com.metavize.gui.store.*;
import com.metavize.gui.transform.*;
import com.metavize.gui.upgrade.*;
import com.metavize.gui.util.*;
import com.metavize.mvvm.*;
import com.metavize.mvvm.security.*;
import com.metavize.mvvm.tran.*;


public class MMainJFrame extends javax.swing.JFrame {

    final int UPGRADE_THREAD_SLEEP_MILLIS = 60 * (60 * 1000); // X * (minutes)

    private static MvvmContext mvvmContext;
    private static ToolboxManager toolboxManager;
    private static TransformManager transformManager;

    private static final Dimension MIN_SIZE = new Dimension(1024, Util.determineMinHeight(768));
    private static final Dimension MAX_SIZE = new Dimension(1600, 1200);
    private GridBagConstraints gridBagConstraints;
    private Hashtable storeHashtable, toolboxHashtable;
    private Object lock;
    private UpdateCheckThread updateCheckThread;
    private JProgressBar statusJProgressBar;


    /** Creates new form asdf */
    public MMainJFrame() {
        this.statusJProgressBar = Util.getStatusJProgressBar();
        Util.setMMainJFrame(this);
        mvvmContext = Util.getMvvmContext();
        toolboxManager = mvvmContext.toolboxManager();
        transformManager = mvvmContext.transformManager();
        storeHashtable = new Hashtable();
        toolboxHashtable = new Hashtable();
        gridBagConstraints = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1d, 0d, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,1,3,3), 0, 0);
        MTransformJButton mTransformJButton;

        // INIT GUI
        initComponents();
	java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1024)/2, (screenSize.height-768)/2, 1024, 768)
;


        Util.updateDependencies();


        // QUERY AND LOAD BUTTONS INTO TOOLBOX
        statusJProgressBar.setString("populating Toolbox...");
        MackageDesc[] installedMackages = toolboxManager.installed();
        Tid[] transformInstances;
        String transformName;

        for(int i=0; i<installedMackages.length; i++){
            if( installedMackages[i].getType() != MackageDesc.TRANSFORM_TYPE )
                continue;

            transformName = installedMackages[i].getName();
            if( transformName.equals("http-transform") || transformName.equals("virus-transform") )
                continue;

            statusJProgressBar.setValue(64 + (int) ((((float)i)/(float)installedMackages.length)*16f) );
            mTransformJButton = new MTransformJButton(installedMackages[i]);
            toolboxHashtable.put( transformName, mTransformJButton );
            //mTransformJButton.setMessage("(ready to install)");
            mTransformJButton.setMessage(null);
            // IF THE TRANSFORM IS ALREADY INSTANTIATED, MAKE SURE THE BUTTON CANNOT BE CLICKED AGAIN
            transformInstances = transformManager.transformInstances( transformName );
            if(  transformInstances != null && transformInstances.length > 0){
                //mTransformJButton.setMessage("(installed)");
                mTransformJButton.setMessage(null);
                mTransformJButton.setEnabled(false);
                mTransformJButton.setTT("Successfully installed...");
            }
            else{
                mTransformJButton.setTT("Ready to be installed...");
            }
            this.addMTransformJButtonToToolbox( mTransformJButton );
        }
    statusJProgressBar.setValue(80);



        // QUERY AND LOAD BUTTONS INTO STORE
        statusJProgressBar.setString("populating Store...");
        MackageDesc[] storeMackages = toolboxManager.uninstalled();

        for(int i=0; i<storeMackages.length; i++){
            if( storeMackages[i].getType() != MackageDesc.TRANSFORM_TYPE )
                continue;
            if( storeMackages[i].getName().equals("http-transform") || storeMackages[i].getName().equals("virus-transform") )
                continue;

            statusJProgressBar.setValue(80 + (int) ((((float)i)/(float)storeMackages.length)*16f) );
            if(!toolboxHashtable.containsKey(storeMackages[i].getName())){
                mTransformJButton = new MTransformJButton(storeMackages[i]);
                //mTransformJButton.setMessage("(available)");
                mTransformJButton.setMessage(null);
                mTransformJButton.setTT("Ready for procurement...");
                storeHashtable.put(storeMackages[i].getName(), mTransformJButton);
                this.addMTransformJButtonToStore(mTransformJButton);
            }
        }
    statusJProgressBar.setValue(96);





        // INITIALIZE GUI
        // statusJProgressBar.setString("Initializing user interface");
        // statusJProgressBar.setValue(64);
        //((MPipelineJPanel)mPipelineJPanel).updateView();
        storeJScrollPane.getVerticalScrollBar().setUnitIncrement(5);
        toolboxJScrollPane.getVerticalScrollBar().setUnitIncrement(5);
        configurationJScrollPane.getVerticalScrollBar().setUnitIncrement(5);


        // UPDATE/UPGRADE
        updateCheckThread = new UpdateCheckThread();
        updateCheckThread.setContextClassLoader( Util.getClassLoader() );
        updateCheckThread.start();
    }

    public void setButtonEnabled(String transformName, boolean enabled){
        MTransformJButton targetButton = (MTransformJButton) toolboxHashtable.get(transformName);
        if(targetButton == null)
            return;

        targetButton.setMessage(null);
        targetButton.setTT("Ready to be installed...");
        /*
          if(enabled)
          targetButton.setMessage("(ready to install)");
          else
          targetButton.setMessage("(installed)");
        **/
        targetButton.setEnabled(enabled);
    }




    public void updateJButton(final int count){
    SwingUtilities.invokeLater( new Runnable() {
        public void run() {
            if( count == 0 ){
            upgradeJButton.setText("<html><center>Upgrade<br>(no upgrades)</center></html>");
            upgradeJButton.setEnabled(true);
            upgradeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/upgrade/IconUnavailable32x32.png")));
            }
            else if( count == 1 ){
            upgradeJButton.setText("<html><center><b>Upgrade<br>(1 upgrade)</b></center></html>");
            upgradeJButton.setEnabled(true);
            upgradeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/upgrade/IconAvailable32x32.png")));
            }
            else if( count > 1){
            upgradeJButton.setText("<html><center><b>Upgrade<br>(" + count + " upgrades)</b></center></html>");
            upgradeJButton.setEnabled(true);
            upgradeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/upgrade/IconAvailable32x32.png")));
            }
            else if( count == -1){
            upgradeJButton.setText("<html><center>Upgrade<br>(unavailable)</center></html>");
            upgradeJButton.setEnabled(true);
            }
            else if( count == -2 ){
            upgradeJButton.setText("<html><center>Upgrade<br>(connecting...)</center></html>");
            upgradeJButton.setEnabled(true);
            }
            else if( count == -3 ){
            upgradeJButton.setText("<html><center>Upgrade<br>(checking...)</center></html>");
            upgradeJButton.setEnabled(true);
            }
        } } );
    }

    public Dimension getMinimumSize(){
        return MIN_SIZE;
    }


    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        controlsJPanel = new javax.swing.JPanel();
        metavizeJButton = new javax.swing.JButton();
        mTabbedPane = new javax.swing.JTabbedPane();
        storeJPanel = new javax.swing.JPanel();
        storeJScrollPane = new javax.swing.JScrollPane();
        storeScrollJPanel = new javax.swing.JPanel();
        storeSpacerJPanel = new javax.swing.JPanel();
        toolboxJPanel = new javax.swing.JPanel();
        toolboxJScrollPane = new javax.swing.JScrollPane();
        toolboxScrollJPanel = new javax.swing.JPanel();
        toolboxSpacerJPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        configurationJPanel = new javax.swing.JPanel();
        configurationJScrollPane = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        adminJButton = new javax.swing.JButton();
        networkJButton = new javax.swing.JButton();
        backupJButton = new javax.swing.JButton();
        licenseJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        configurationSpacerJPanel1 = new javax.swing.JPanel();
        upgradeJButton = new javax.swing.JButton();
        mPipelineJPanel = new com.metavize.gui.pipeline.MPipelineJPanel();
        backgroundJLabel = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Metavize EdgeGuard Client");
        setFocusCycleRoot(false);
        setIconImage((new javax.swing.ImageIcon( this.getClass().getResource("/com/metavize/gui/icons/LogoNoText16x16.gif"))).getImage());
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        controlsJPanel.setLayout(new java.awt.GridBagLayout());

        controlsJPanel.setFocusable(false);
        controlsJPanel.setMinimumSize(new java.awt.Dimension(200, 427));
        controlsJPanel.setOpaque(false);
        controlsJPanel.setPreferredSize(new java.awt.Dimension(200, 410));
        metavizeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/main/LogoNoText96x96.png")));
        metavizeJButton.setBorderPainted(false);
        metavizeJButton.setContentAreaFilled(false);
        metavizeJButton.setDoubleBuffered(true);
        metavizeJButton.setFocusPainted(false);
        metavizeJButton.setFocusable(false);
        metavizeJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        metavizeJButton.setMargin(new java.awt.Insets(1, 3, 3, 3));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(45, 45, 45, 45);
        controlsJPanel.add(metavizeJButton, gridBagConstraints);

        mTabbedPane.setDoubleBuffered(true);
        mTabbedPane.setFocusable(false);
        mTabbedPane.setFont(new java.awt.Font("Arial", 0, 11));
        mTabbedPane.setMinimumSize(new java.awt.Dimension(177, 177));
        mTabbedPane.setPreferredSize(new java.awt.Dimension(200, 160));
        storeJPanel.setLayout(new java.awt.GridBagLayout());

        storeJPanel.setBorder(new javax.swing.border.TitledBorder(null, " Click to Purchase ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        storeJPanel.setFocusable(false);
        storeJPanel.setFont(new java.awt.Font("Arial", 0, 11));
        storeJPanel.setMaximumSize(new java.awt.Dimension(189, 32767));
        storeJPanel.setMinimumSize(new java.awt.Dimension(189, 134));
        storeJPanel.setOpaque(false);
        storeJScrollPane.setBorder(null);
        storeJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        storeJScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        storeJScrollPane.setDoubleBuffered(true);
        storeJScrollPane.setFocusable(false);
        storeJScrollPane.setFont(new java.awt.Font("Arial", 0, 12));
        storeJScrollPane.setOpaque(false);
        storeJScrollPane.getViewport().setOpaque(false);
        storeScrollJPanel.setLayout(new java.awt.GridBagLayout());

        storeScrollJPanel.setFocusable(false);
        storeScrollJPanel.setOpaque(false);
        storeSpacerJPanel.setFocusable(false);
        storeSpacerJPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        storeScrollJPanel.add(storeSpacerJPanel, gridBagConstraints);

        storeJScrollPane.setViewportView(storeScrollJPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        storeJPanel.add(storeJScrollPane, gridBagConstraints);

        mTabbedPane.addTab("Store", storeJPanel);

        toolboxJPanel.setLayout(new java.awt.GridBagLayout());

        toolboxJPanel.setBorder(new javax.swing.border.TitledBorder(null, " Click to Install into Rack", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        toolboxJPanel.setFocusable(false);
        toolboxJPanel.setMaximumSize(new java.awt.Dimension(189, 32767));
        toolboxJPanel.setMinimumSize(new java.awt.Dimension(189, 134));
        toolboxJPanel.setOpaque(false);
        toolboxJScrollPane.setBorder(null);
        toolboxJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        toolboxJScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        toolboxJScrollPane.setDoubleBuffered(true);
        toolboxJScrollPane.setFocusable(false);
        toolboxJScrollPane.setOpaque(false);
        toolboxJScrollPane.getViewport().setOpaque(false);
        toolboxScrollJPanel.setLayout(new java.awt.GridBagLayout());

        toolboxScrollJPanel.setFocusable(false);
        toolboxScrollJPanel.setOpaque(false);
        toolboxSpacerJPanel.setFocusable(false);
        toolboxSpacerJPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        toolboxScrollJPanel.add(toolboxSpacerJPanel, gridBagConstraints);

        toolboxJScrollPane.setViewportView(toolboxScrollJPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        toolboxJPanel.add(toolboxJScrollPane, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel1.setText("shift-click to remove...");
        jLabel1.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        toolboxJPanel.add(jLabel1, gridBagConstraints);

        mTabbedPane.addTab("Toolbox", toolboxJPanel);

        configurationJPanel.setLayout(new java.awt.GridBagLayout());

        configurationJPanel.setBorder(new javax.swing.border.TitledBorder(null, " Click to Configure", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11)));
        configurationJPanel.setFocusable(false);
        configurationJPanel.setMaximumSize(new java.awt.Dimension(189, 134));
        configurationJPanel.setMinimumSize(new java.awt.Dimension(189, 134));
        configurationJPanel.setOpaque(false);
        configurationJPanel.setPreferredSize(new java.awt.Dimension(189, 134));
        configurationJScrollPane.setBorder(null);
        configurationJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        configurationJScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        configurationJScrollPane.setDoubleBuffered(true);
        configurationJScrollPane.setFocusable(false);
        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel8.setFocusable(false);
        adminJButton.setFont(new java.awt.Font("Arial", 0, 12));
        adminJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/icons/LogoNoText32x32.png")));
        adminJButton.setText("<html>Administrator<br>Accounts</html>");
        adminJButton.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 0))));
        adminJButton.setDoubleBuffered(true);
        adminJButton.setFocusPainted(false);
        adminJButton.setFocusable(false);
        adminJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        adminJButton.setMargin(new java.awt.Insets(1, 3, 4, 2));
        adminJButton.setMaximumSize(new java.awt.Dimension(810, 370));
        adminJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 3, 3);
        jPanel8.add(adminJButton, gridBagConstraints);

        networkJButton.setFont(new java.awt.Font("Arial", 0, 12));
        networkJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/icons/LogoNoText32x32.png")));
        networkJButton.setText("<html>Advanced<br>Network Settings</html>");
        networkJButton.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 0))));
        networkJButton.setDoubleBuffered(true);
        networkJButton.setFocusPainted(false);
        networkJButton.setFocusable(false);
        networkJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        networkJButton.setMargin(new java.awt.Insets(1, 3, 4, 2));
        networkJButton.setMaximumSize(new java.awt.Dimension(810, 370));
        networkJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                networkJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 3, 3);
        jPanel8.add(networkJButton, gridBagConstraints);

        backupJButton.setFont(new java.awt.Font("Arial", 0, 12));
        backupJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/icons/LogoNoText32x32.png")));
        backupJButton.setText("<html>Backup and<br>Restore</html>");
        backupJButton.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 0))));
        backupJButton.setDoubleBuffered(true);
        backupJButton.setFocusPainted(false);
        backupJButton.setFocusable(false);
        backupJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        backupJButton.setMargin(new java.awt.Insets(1, 3, 4, 2));
        backupJButton.setMaximumSize(new java.awt.Dimension(810, 370));
        backupJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backupJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 3, 3);
        jPanel8.add(backupJButton, gridBagConstraints);

        licenseJButton.setFont(new java.awt.Font("Arial", 0, 12));
        licenseJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/icons/LogoNoText32x32.png")));
        licenseJButton.setText("<html>License<br>Agreement</html>");
        licenseJButton.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 0))));
        licenseJButton.setDoubleBuffered(true);
        licenseJButton.setFocusPainted(false);
        licenseJButton.setFocusable(false);
        licenseJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        licenseJButton.setMargin(new java.awt.Insets(1, 3, 4, 2));
        licenseJButton.setMaximumSize(new java.awt.Dimension(810, 370));
        licenseJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 3, 3);
        jPanel8.add(licenseJButton, gridBagConstraints);

        aboutJButton.setFont(new java.awt.Font("Arial", 0, 12));
        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/icons/LogoNoText32x32.png")));
        aboutJButton.setText("<html>About<br>EdgeGuard</html>");
        aboutJButton.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 0))));
        aboutJButton.setDoubleBuffered(true);
        aboutJButton.setFocusPainted(false);
        aboutJButton.setFocusable(false);
        aboutJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        aboutJButton.setMargin(new java.awt.Insets(1, 3, 4, 2));
        aboutJButton.setMaximumSize(new java.awt.Dimension(810, 370));
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 3, 3);
        jPanel8.add(aboutJButton, gridBagConstraints);

        configurationSpacerJPanel1.setFocusable(false);
        configurationSpacerJPanel1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel8.add(configurationSpacerJPanel1, gridBagConstraints);

        configurationJScrollPane.setViewportView(jPanel8);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        configurationJPanel.add(configurationJScrollPane, gridBagConstraints);

        mTabbedPane.addTab("Config", configurationJPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        controlsJPanel.add(mTabbedPane, gridBagConstraints);

        upgradeJButton.setFont(new java.awt.Font("Default", 0, 12));
        upgradeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/upgrade/IconAvailable32x32.png")));
        upgradeJButton.setText("<html>Upgrade<br></html>");
        upgradeJButton.setDoubleBuffered(true);
        upgradeJButton.setFocusPainted(false);
        upgradeJButton.setFocusable(false);
        upgradeJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        upgradeJButton.setMargin(new java.awt.Insets(2, 2, 2, 4));
        upgradeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upgradeJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 20, 10, 20);
        controlsJPanel.add(upgradeJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(controlsJPanel, gridBagConstraints);

        mPipelineJPanel.setOpaque(false);
        //((com.metavize.gui.pipeline.MPipelineJPanel)mPipelineJPanel).setMFilterJPanel((com.metavize.gui.filter.MFilterJPanel)mFilterJPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mPipelineJPanel, gridBagConstraints);

        backgroundJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/main/MainBackground1600x1200.png")));
        backgroundJLabel.setDoubleBuffered(true);
        backgroundJLabel.setFocusable(false);
        backgroundJLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(backgroundJLabel, gridBagConstraints);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1024)/2, (screenSize.height-768)/2, 1024, 768);
    }//GEN-END:initComponents

    private void backupJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupJButtonActionPerformed
        new BackupRestoreThread();
    }//GEN-LAST:event_backupJButtonActionPerformed

    private void networkJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_networkJButtonActionPerformed
        new NetworkThread();
    }//GEN-LAST:event_networkJButtonActionPerformed

    private void licenseJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseJButtonActionPerformed
        new LicenseThread();
    }//GEN-LAST:event_licenseJButtonActionPerformed

    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        new AboutThread();
    }//GEN-LAST:event_aboutJButtonActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        Util.resizeCheck(this, MIN_SIZE, MAX_SIZE);
    }//GEN-LAST:event_formComponentResized

    private void upgradeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upgradeJButtonActionPerformed
    new UpgradeThread();
    }//GEN-LAST:event_upgradeJButtonActionPerformed

    public void addMTransformJButtonToStore(MTransformJButton mTransformJButton){
        if(mTransformJButton == null)
            return;

        mTransformJButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    storeActionPerformed(evt);
                }
            });

        storeScrollJPanel.add(mTransformJButton, gridBagConstraints, 0);
        storeJScrollPane.getVerticalScrollBar().setValue(0);
        mTabbedPane.setSelectedIndex(0);
    }

    public void addMTransformJButtonToToolbox(MTransformJButton mTransformJButton){
        if(mTransformJButton == null)
            return;

        mTransformJButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    toolboxActionPerformed(evt);
                }
            });

        toolboxScrollJPanel.add(mTransformJButton, gridBagConstraints, 0);
        toolboxJScrollPane.getVerticalScrollBar().setValue(0);
        mTabbedPane.setSelectedIndex(1);
    }

    private void storeActionPerformed(java.awt.event.ActionEvent evt){
        new StoreThread(evt);
    }


    private void toolboxActionPerformed(java.awt.event.ActionEvent evt){
        MTransformJButton mTransformJButton = ((MTransformJButton) evt.getSource());
        if( (evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
            mTransformJButton.uninstall(storeHashtable, toolboxHashtable, storeScrollJPanel, toolboxScrollJPanel, mTabbedPane);
        }
        else{
            mTransformJButton.install();
        }
    }


    private void adminJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminJButtonActionPerformed
        new AdminConfigThread();
    }//GEN-LAST:event_adminJButtonActionPerformed


    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        Util.exit(0);
    }//GEN-LAST:event_exitForm
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton adminJButton;
    private javax.swing.JLabel backgroundJLabel;
    private javax.swing.JButton backupJButton;
    private javax.swing.JPanel configurationJPanel;
    private javax.swing.JScrollPane configurationJScrollPane;
    private javax.swing.JPanel configurationSpacerJPanel1;
    private javax.swing.JPanel controlsJPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JButton licenseJButton;
    private javax.swing.JPanel mPipelineJPanel;
    private javax.swing.JTabbedPane mTabbedPane;
    private javax.swing.JButton metavizeJButton;
    private javax.swing.JButton networkJButton;
    private javax.swing.JPanel storeJPanel;
    private javax.swing.JScrollPane storeJScrollPane;
    private javax.swing.JPanel storeScrollJPanel;
    private javax.swing.JPanel storeSpacerJPanel;
    private javax.swing.JPanel toolboxJPanel;
    private javax.swing.JScrollPane toolboxJScrollPane;
    private javax.swing.JPanel toolboxScrollJPanel;
    private javax.swing.JPanel toolboxSpacerJPanel;
    private javax.swing.JButton upgradeJButton;
    // End of variables declaration//GEN-END:variables

    private class BackupRestoreThread extends Thread {
	public BackupRestoreThread(){
	    //backupJButton.setEnabled(false);
	    //(new Thread(this)).start();
            run();
	}
        public void run(){
            try{
                BackupRestoreJDialog backupRestoreJDialog = new BackupRestoreJDialog();
                backupRestoreJDialog.setBounds( Util.generateCenteredBounds(Util.getMMainJFrame().getBounds(), backupRestoreJDialog.getWidth(), backupRestoreJDialog.getHeight()) );
                backupRestoreJDialog.setVisible(true);
            }
            catch(Exception e){
                try{ Util.handleExceptionWithRestart("Error showing backup and restore panel", e); }
                catch(Exception f){
                    Util.handleExceptionNoRestart("Error showing backup and restore panel", f);
                }
            }
	    finally{
		//backupJButton.setEnabled(true);
	    }
        }
    }
    
    private class NetworkThread extends Thread {
	public NetworkThread(){
	    //networkJButton.setEnabled(false);
	    //(new Thread(this)).start();
            run();
	}
        public void run(){
            try{
                NetworkJDialog networkJDialog = new NetworkJDialog();
                networkJDialog.setBounds( Util.generateCenteredBounds(Util.getMMainJFrame().getBounds(), networkJDialog.getWidth(), networkJDialog.getHeight()) );
                networkJDialog.setVisible(true);
            }
            catch(Exception e){
                try{ Util.handleExceptionWithRestart("Error showing network settings", e); }
                catch(Exception f){
                    Util.handleExceptionNoRestart("Error showing network settings", f);
                }
            }
	    finally{
		//networkJButton.setEnabled(true);
	    }
        }
    }
        
    private class LicenseThread extends Thread {
    public LicenseThread(){
        //licenseJButton.setEnabled(false);
        //(new Thread(this)).start();
        run();
    }
        public void run(){
            try{
                LicenseJDialog licenseJDialog = new LicenseJDialog();
                licenseJDialog.setBounds( Util.generateCenteredBounds(Util.getMMainJFrame().getBounds(), licenseJDialog.getWidth(), licenseJDialog.getHeight()) );
                licenseJDialog.setVisible(true);
            }
            catch(Exception e){
                try{ Util.handleExceptionWithRestart("Error showing license", e); }
                catch(Exception f){
                    Util.handleExceptionNoRestart("Error showing license", f);
                }
            }
        finally{
            //licenseJButton.setEnabled(true);
        }
        }
    }


    private class AboutThread extends Thread {
    public AboutThread(){
        //aboutJButton.setEnabled(false);
        //(new Thread(this)).start();
        run();
    }
        public void run(){
            try{
                AboutJDialog aboutJDialog = new AboutJDialog();
                aboutJDialog.setBounds( Util.generateCenteredBounds(Util.getMMainJFrame().getBounds(), aboutJDialog.getWidth(), aboutJDialog.getHeight()) );
                aboutJDialog.setVisible(true);

            }
            catch(Exception e){
                try{ Util.handleExceptionWithRestart("Error showing about", e); }
                catch(Exception f){
                    Util.handleExceptionNoRestart("Error showing about", f);
                }
            }
        finally{
            //aboutJButton.setEnabled(true);
        }
        }
    }


    private class AdminConfigThread extends Thread {
    public AdminConfigThread(){
        //adminJButton.setEnabled(false);
        //(new Thread(this)).start();
        run();
    }
        public void run(){
            try{
                AdminConfigJDialog adminConfigJDialog = new AdminConfigJDialog();
                adminConfigJDialog.setBounds( Util.generateCenteredBounds(Util.getMMainJFrame().getBounds(), adminConfigJDialog.getWidth(), adminConfigJDialog.getHeight()) );
                adminConfigJDialog.setVisible(true);

            }
            catch(Exception e){
                try{ Util.handleExceptionWithRestart("Error changing admins", e); }
                catch(Exception f){
                    Util.handleExceptionNoRestart("Error changing admins", f);
                }
            }
        finally{
            //adminJButton.setEnabled(true);
        }
        }
    }

    private class StoreThread extends Thread {
        ActionEvent event;
        public StoreThread(ActionEvent event){
        this.event = event;
        ((MTransformJButton) event.getSource()).setEnabled(false);
        (new Thread(this)).start();
    }
        public void run(){
            MTransformJButton mTransformJButton = null;
            MStoreJDialog mStoreJDialog = new MStoreJDialog(MMainJFrame.this, true, ((MTransformJButton) event.getSource()).duplicate());
            mStoreJDialog.setBounds( Util.generateCenteredBounds(MMainJFrame.this.getBounds(), mStoreJDialog.getWidth(), mStoreJDialog.getHeight()) );
            mStoreJDialog.setVisible(true);
            mTransformJButton = mStoreJDialog.getPurchasedMTransformJButton();

            if(mTransformJButton != null){
                mTransformJButton = ((MTransformJButton) event.getSource());
                mTransformJButton.purchase(storeHashtable, toolboxHashtable, storeScrollJPanel, toolboxScrollJPanel, mTabbedPane);
            }
        else{
        ((MTransformJButton) event.getSource()).setEnabled(true);
        }
        }
    }

    private class UpgradeThread extends Thread {
    public UpgradeThread(){
        upgradeJButton.setEnabled(false);
        (new Thread(this)).start();
    }
        public void run(){
            try{
                ToolboxManager toolboxManager;
                MackageDesc[] mackageDesc = null;
                toolboxManager = mvvmContext.toolboxManager();
                UpgradeJDialog upgradeJDialog =  new UpgradeJDialog(MMainJFrame.this, toolboxManager);
                upgradeJDialog.setBounds( Util.generateCenteredBounds(MMainJFrame.this.getBounds(), upgradeJDialog.getWidth(), upgradeJDialog.getHeight()) );
                upgradeJDialog.update();
                upgradeJDialog.setVisible(true);
                mackageDesc = toolboxManager.upgradable();

                if( (mackageDesc == null) || (mackageDesc.length == 0) )
                    updateJButton(0);
                else
                    updateJButton(mackageDesc.length);
            }
            catch(Exception e){
                try{ Util.handleExceptionWithRestart("Error checking for upgrades on server", e); }
                catch(Exception f){
                    Util.handleExceptionNoRestart("Error checking for upgrades on server", f);
                    updateJButton(-1);
                }
            }
        finally{
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                upgradeJButton.setEnabled(true);
            } } );
        }
        }
    }


    private class UpdateCheckThread extends Thread {
        private MackageDesc[] mackageDesc;
        public void UpdateCheckThread(){
            this.setDaemon(true);
        }
        public void run() {

            try{
                ToolboxManager toolboxManager;

                while(true){
                    if(Util.getKillThreads())
                        return;
                    updateJButton(-2);
                    toolboxManager = mvvmContext.toolboxManager();
                    // toolboxManager.update();

                    updateJButton(-3);
                    mackageDesc = toolboxManager.upgradable();
                    if( (mackageDesc == null) || (mackageDesc.length == 0) )
                        updateJButton(0);
                    else
                        updateJButton(mackageDesc.length);
                    Thread.sleep(UPGRADE_THREAD_SLEEP_MILLIS);
                }
            }
            catch(Exception e){  // handle exception more gracefully
                //try{ Util.handleExceptionWithRestart("Error checking for upgrades on server", e); }
                //catch(Exception f){
                Util.handleExceptionNoRestart("Error auto checking for upgrades on server", e);
                updateJButton(-1);
                //   return;
                //}
            }
        }
    }


}



