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

package com.untangle.tran.spyware;


import com.untangle.mvvm.tapi.TCPNewSessionRequest;
import com.untangle.mvvm.tapi.TCPSession;
import com.untangle.tran.token.TokenHandler;
import com.untangle.tran.token.TokenHandlerFactory;
import org.apache.log4j.Logger;

class SpywareHttpFactory implements TokenHandlerFactory
{
    private final Logger logger = Logger.getLogger(getClass());

    private final SpywareImpl transform;

    // constructors -----------------------------------------------------------

    SpywareHttpFactory(SpywareImpl transform)
    {
        this.transform = transform;
    }

    // TokenHandlerFactory methods --------------------------------------------

    public TokenHandler tokenHandler(TCPSession session)
    {
        return new SpywareHttpHandler(session, transform);
    }

    public void handleNewSessionRequest(TCPNewSessionRequest tsr)
    {
    }
}
