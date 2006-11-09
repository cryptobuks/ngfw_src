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

package com.untangle.mvvm.api;

import com.untangle.mvvm.IntfEnum;

public interface RemoteIntfManager
{
    /* Retrieve the current interface enumeration */
    public IntfEnum getIntfEnum();
}