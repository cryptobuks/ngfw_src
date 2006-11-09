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
package com.untangle.mvvm.util;

public class ReportingOutsideAccessValve extends OutsideValve
{
    public ReportingOutsideAccessValve()
    {
    }
    
    protected boolean isOutsideAccessAllowed()
    {
        return getRemoteSettings().getIsOutsideReportingEnabled();
    }

    protected String errorMessage()
    {
        return "Off-site access to reporting is disabled.";
    }
}