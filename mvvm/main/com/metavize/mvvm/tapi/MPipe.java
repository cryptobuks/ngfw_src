/*
 * Copyright (c) 2003, 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id: MPipe.java,v 1.16 2005/03/22 03:48:36 amread Exp $
 */

package com.metavize.mvvm.tapi;

import com.metavize.mvvm.argon.ArgonAgent;
import com.metavize.mvvm.tapi.event.SessionEventListener;
import com.metavize.mvvm.tran.Transform;


/**
 * The <code>MPipe</code> interface represents an active MetaPipe.
 * Most transforms only have one active <code>MPipe</code> at a time,
 * the rest have exactly 2 (casings).
 *
 * This class's instances represent and contain the subscription
 * state, pipeline state, and accessors to get the live sessions for
 * the pipe, as well as
 *
 * This used to be called 'Xenon'.
 *
 * @author <a href="mailto:jdi@SLAB"></a>
 * @version 1.0
 */
public interface MPipe {

    /**
     * Deactivates an active MetaPipe and disconnects it from argon.
     * This kills all sessions and threads, and keeps any new sessions
     * or further commands from being issued.
     *
     * The xenon may not be used again.  State will be
     * <code>DEAD_ARGON</code> from here on out.
     */
    void destroy();

    PipeSpec getPipeSpec();

    void setSessionEventListener(SessionEventListener listener);

    int[] liveSessionIds();

    IPSessionDesc[] liveSessionDescs();

    void dumpSessions();

    Transform transform();

    ArgonAgent getArgonAgent();

    LiveSubscription addSubscription(Subscription desc);

    void removeSubscription(LiveSubscription toRemove);

    // Returns an array of length 0 if no subscriptions active.
    LiveSubscription[] subscriptions();

    // Removes all subscription
    void clearSubscriptions();

    // disconnect?
    // void closeClientChannel(TCPSession session);
    // void closeServerChannel(TCPSession session);

    // void scheduleTimer(IPSessionImpl session, long delay);
    // void cancelTimer(IPSessionImpl session);
}


