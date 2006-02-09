/*
 * Copyright (c) 2005, 2006 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */

package com.metavize.tran.httpblocker;


import com.metavize.mvvm.logging.RepositoryDesc;
import com.metavize.mvvm.logging.SimpleEventFilter;

public class HttpBlockerWhitelistFilter
    implements SimpleEventFilter<HttpBlockerEvent>
{
    private static final RepositoryDesc REPO_DESC
        = new RepositoryDesc("Whitelisted HTTP Traffic");

    private static final String WARM_QUERY
        = "FROM HttpBlockerEvent evt WHERE evt.action = 'P' AND evt.requestLine.pipelineEndpoints.policy = :policy ORDER BY evt.timeStamp DESC";

    // SimpleEventFilter methods ----------------------------------------------

    public RepositoryDesc getRepositoryDesc()
    {
        return REPO_DESC;
    }

    public String[] getQueries()
    {
        return new String[] { WARM_QUERY };
    }

    public boolean accept(HttpBlockerEvent e)
    {
        return e.isPersistent() && Action.PASS == e.getAction();
    }
}
