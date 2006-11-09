/*
 * Copyright (c) 2003-2006 Untangle, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Untangle, Inc. ("Confidential Information"). You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */


package com.untangle.tran.openvpn.gui;

import com.untangle.gui.transform.*;
import com.untangle.gui.pipeline.MPipelineJPanel;
import com.untangle.mvvm.tran.TransformContext;


public class MTransformDisplayJPanel extends com.untangle.gui.transform.MTransformDisplayJPanel{
    

    
    public MTransformDisplayJPanel(MTransformJPanel mTransformJPanel) throws Exception {
        super(mTransformJPanel);
        
        super.activity0JLabel.setText("BLOCK");
        super.activity1JLabel.setText("PASS");
        super.activity2JLabel.setText("CLIENT");
        //super.activity3JLabel.setText("TEST 4");

        // XXX Won't work until made public
        // throughputJLabel.setText("active clients");
    }
    
}
