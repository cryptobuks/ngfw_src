/*
 * Copyright (c) 2003 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id: Range.java,v 1.4 2005/01/12 02:52:56 rbscott Exp $
 */

package com.metavize.jnetcap;

public class Range {
    private final int low;
    private final int high;

    /**
     * Create a range from <code>low</code> to <code>high<code>
     * @param low - low value (inclusive).
     * @param high - high value (inclusive).
     */
    public Range ( int low, int high )
    {
        if ( low > high ) {
            throw new IllegalArgumentException( "Reversed port range: low=" + low + " high=" + high );
        }
        
        this.low  = low;
        this.high = high;
    }

    /**
     * Retrieve the low value
     */
    public int low() 
    {
        return low;
    }

    /**
     * Retrieve the high value
     */
    public int high()
    {
        return high;
    }
}
