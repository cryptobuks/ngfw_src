/*
 * Copyright (c) 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.gui.configuration;

import com.metavize.gui.widgets.dialogs.*;
import com.metavize.gui.widgets.editTable.*;
import com.metavize.gui.util.*;

import com.metavize.mvvm.NetworkingConfiguration;

import java.awt.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;



public class NetworkJDialog extends MConfigJDialog {
    
    private static final String NAME_NETWORKING_CONFIG = "Networking Config";
    private static final String NAME_NETWORK_SETTINGS = "External Address";
    private static final String NAME_ALIAS_PANEL      = "External Address Aliases";
    private static final String NAME_TIMEZONE_PANEL      = "Timezone";

    public NetworkJDialog( ) {
    }

    protected Dimension getMinSize(){
	return new Dimension(640, 550);
    }
    
    protected void generateGui(){
        this.setTitle(NAME_NETWORKING_CONFIG);
        
        // NETWORK SETTINGS //////
        NetworkIPJPanel ipJPanel = new NetworkIPJPanel();
	addScrollableTab(null, NAME_NETWORK_SETTINGS, null, ipJPanel, false, true);
	addSavable(NAME_NETWORK_SETTINGS, ipJPanel);
	addRefreshable(NAME_NETWORK_SETTINGS, ipJPanel);
        	
        // ALIASES /////
        NetworkAliasJPanel aliasJPanel = new NetworkAliasJPanel();
	addTab(NAME_ALIAS_PANEL, null, aliasJPanel );
	addSavable(NAME_ALIAS_PANEL, aliasJPanel );
	addRefreshable(NAME_ALIAS_PANEL, aliasJPanel );

	// NETWORK SETTINGS //////
        NetworkTimezoneJPanel timezoneJPanel = new NetworkTimezoneJPanel();
	addScrollableTab(null, NAME_TIMEZONE_PANEL, null, timezoneJPanel, false, true);
	addSavable(NAME_TIMEZONE_PANEL, timezoneJPanel);
	addRefreshable(NAME_TIMEZONE_PANEL, timezoneJPanel);
    }
    
    protected void sendSettings(Object settings) throws Exception {
	Util.getNetworkingManager().set( (NetworkingConfiguration) settings);
        
    }
    protected void refreshSettings(){
	settings = Util.getNetworkingManager().get();
    }

    protected void saveAll(){
	// ASK THE USER IF HE REALLY WANTS TO SAVE SETTINGS ////////
        NetworkSaveSettingsProceedJDialog networkSaveSettingsProceedJDialog = new NetworkSaveSettingsProceedJDialog();
        boolean isProceeding = networkSaveSettingsProceedJDialog.isProceeding();
        if( isProceeding ){ 
            super.saveAll();
	    // UPDATE STORE
	    Util.getPolicyStateMachine().updateStoreModel();
        }
    }

}
