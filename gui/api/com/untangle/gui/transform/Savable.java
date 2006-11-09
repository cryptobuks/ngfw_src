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

package com.untangle.gui.transform;

import com.untangle.gui.util.*;

public interface Savable<T> {

    public void doSave(T compoundSettings, boolean validateOnly) throws Exception;

}
