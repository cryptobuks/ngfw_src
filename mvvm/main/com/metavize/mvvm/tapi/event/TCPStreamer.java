/*
 * Copyright (c) 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: TCPStreamer.java,v 1.1 2005/01/21 19:35:23 jdi Exp $
 */

package com.metavize.mvvm.tapi.event;

import java.nio.ByteBuffer;

public interface TCPStreamer extends IPStreamer
{
    /**
     * <code>nextChunk</code> should return a ByteBuffer containing the next chunk to
     * be sent.  (Bytes are sent from the buffer's position to its limit).  The buffer
     * should contain a reasonable amount of data for good performance, anywhere from
     * 2K to 16K is usual.  Returns null on EOF.
     *
     * @return a <code>ByteBuffer</code> giving the bytes of the next chunk to send.  Null when EOF
     */
    ByteBuffer nextChunk();
}
