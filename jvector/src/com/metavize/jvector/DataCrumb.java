/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: DataCrumb.java,v 1.6 2005/01/10 23:21:32 rbscott Exp $
 */

package com.metavize.jvector;

public class DataCrumb extends Crumb
{
    /* Data can point to a byte array that is larger than the actual amount of data read,
     * size contains the actual amount of data in the crumb */
    protected final byte[] data;
    protected int limit;
    protected int offset;

    public DataCrumb( byte[] data, int offset, int limit )
    {
        this.data = data;
        limit( limit );

        /** You have to set offset after limit */
        offset( offset );
    }

    public DataCrumb( byte[] data, int limit )
    {
        this( data, 0, limit );
    }

    public DataCrumb( byte[] data )
    {
        this( data, data.length );
    }

    public DataCrumb( String data )
    {
        this( data.getBytes());
    }

    public int    type()   { return TYPE_DATA; }

    public byte[] data()   { return data;   }
    public int    limit()  { return limit;  }
    public int    offset() { return offset; }

    /**
     * Change the limit of the data crumb */
    public void limit( int limit ) 
    {
        if ( limit > data.length ) {
            throw new IllegalArgumentException( "Limit is larger than the underlying byte array" );
        }
        this.limit = limit;
    }

    /**
     * Change the offset of the data crumb.</p>
     * @param value - The new offset of the data crumb
     */
    public void offset( int offset )
    {
        if ( offset > limit ) {
            throw new IllegalArgumentException( "Setting offset(" + offset + ") passed the end of the " +
                                                "data crumb(" + limit + ")" );
        }
        
        this.offset = offset;        
    }
    
    void   advance( int amount ) 
    {
        offset( offset + amount );
    }

    public void raze() {
        /* XXX What should go in here, C structure is freed automatically */
    }
}
