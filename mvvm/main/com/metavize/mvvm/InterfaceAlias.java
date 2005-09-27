/*
 * Copyright (c) 2003, 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id$
 */

package com.metavize.mvvm;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Inet4Address;

import com.metavize.mvvm.tran.IPaddr;
import static com.metavize.mvvm.NetworkingConfiguration.EMPTY_IPADDR;

public class InterfaceAlias implements Serializable
{
    private static final long serialVersionUID = -2103291468092590446L;

    private IPaddr address;
    private IPaddr netmask;
    // Presently unused, but settable */
    private IPaddr broadcast;

    public InterfaceAlias()
    {
        this.address   = EMPTY_IPADDR;
        this.netmask   = EMPTY_IPADDR;
        this.broadcast = EMPTY_IPADDR;

    }
    
    public InterfaceAlias( IPaddr address, IPaddr netmask )
    {
        this.address   = address;
        this.netmask   = netmask;
        this.broadcast = EMPTY_IPADDR;
    }

    public InterfaceAlias( IPaddr address, IPaddr netmask, IPaddr broadcast )
    {
        this.address   = address;
        this.netmask   = netmask;
        this.broadcast = broadcast;
    }
    
    public InterfaceAlias( InetAddress address, InetAddress netmask, InetAddress broadcast )
    {
        this.address   = new IPaddr((Inet4Address)address );
        this.netmask   = new IPaddr((Inet4Address)netmask );
        this.broadcast = new IPaddr((Inet4Address)broadcast );
    }
    
    public IPaddr getAddress()
    {
        if ( this.address == null || this.address.isEmpty()) this.address = EMPTY_IPADDR;
        return this.address;
    }
    
    public void setAddress( IPaddr address)
    {
        if ( null == address || address.isEmpty()) address = EMPTY_IPADDR;
        this.address = address;
    }

    public IPaddr getNetmask()
    {
        if ( null == this.netmask || this.netmask.isEmpty()) this.netmask = EMPTY_IPADDR;
        return this.netmask;
    }
    
    public void setNetmask( IPaddr netmask)
    {
        if ( null == netmask || netmask.isEmpty()) netmask = EMPTY_IPADDR;
        this.netmask = netmask;
    }

    public boolean isValid()
    {
        if (( null == this.address ) || ( null == this.netmask )) return false;
        if ( this.address.isEmpty() || this.netmask.isEmpty())    return false;
        return true;
    }    
}
