/*
 * Copyright (c) 2003, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: NetcapSession.java,v 1.8 2005/01/31 01:15:12 rbscott Exp $
 */


package com.metavize.jnetcap;

import java.net.InetAddress;

public abstract class NetcapSession {
    /* Pointer to the netcap_session_t structure in netcap */
    protected CPointer pointer;

    private final static int FLAG_ID         = 1;
    private final static int FLAG_PROTOCOL   = 2;

    protected final static int FLAG_IF_CLIENT_MASK = 0x2000;
    protected final static int FLAG_IF_SRC_MASK    = 0x1000;
    

    /* For the following options one of FLAG_ClientMask or FLAG_ServerMask AND FLAG_SrcMask or FLAG_DstMask,
     * must also be set */
    private final static int FLAG_HOST        = 16;
    private final static int FLAG_PORT        = 17;
    private final static int FLAG_INTERFACE   = 18;

    /* This is the mask for the remove the client/server parts */
    private final static int FLAG_MASK        = 0xFFF;

    protected final Endpoints clientSide;
    protected final Endpoints serverSide;

    /* This is for children that override the SessionEndpoints class */
    protected NetcapSession( int id, short protocol )
    {
        pointer = new CPointer( getSession( id, protocol ));

        clientSide = makeEndpoints( true );
        serverSide = makeEndpoints( false );
    }

    /* Returns one of Netcap.IPPROTO_UDP, Netcap.IPPROTO_TCP, Netcap.IPPROTO_ICMP */
    public short protocol() {
        return (short)getIntValue( FLAG_PROTOCOL, pointer.value());
    }

    public int id() {
        return getIntValue( FLAG_ID, pointer.value());
    }

    public void raze() {
        raze( pointer.value());

        pointer.raze();
    }

    protected abstract Endpoints makeEndpoints( boolean ifClient );

    public Endpoints clientSide() { return clientSide; }
    public Endpoints serverSide() { return serverSide; }

    private static native long getSession( int id, short protocol );
    private static native void raze( long session );

    protected static native long   getLongValue  ( int id, long session );
    protected static native int    getIntValue   ( int id, long session );
    protected static native String getStringValue( int id, long session );

    static
    {
        Netcap.load();
    }

    protected class SessionEndpoints implements Endpoints
    {
        protected final boolean ifClientSide;
        protected final Endpoint client;
        protected final Endpoint server;

        SessionEndpoints( boolean ifClientSide ) {
            this.ifClientSide = ifClientSide;
            client = new SessionEndpoint( true );
            server = new SessionEndpoint( false );
        }

        public Endpoint client() { return client; }
        public Endpoint server() { return server; }

        protected class SessionEndpoint implements Endpoint {
            private final boolean ifClient;

            SessionEndpoint( boolean ifClient )
            {
                this.ifClient = ifClient;
            }

            public InetAddress host()
            {
                long addr = getLongValue( buildMask( FLAG_HOST ), pointer.value());

                return Inet4AddressConverter.toAddress( addr );
            }

            public int port()
            {
                return getIntValue( buildMask( FLAG_PORT ), pointer.value());
            }

            public String interfaceName()
            {
                return getStringValue( buildMask( FLAG_INTERFACE ), pointer.value());
            }

            public byte interfaceId()
            {
                return (byte)getIntValue( buildMask( FLAG_INTERFACE ), pointer.value());
            }


            protected int buildMask( int type )
            {
                int mask = ( ifClientSide ) ? FLAG_IF_CLIENT_MASK : 0;
                mask |= ( ifClient ) ? FLAG_IF_SRC_MASK : 0;
                mask |= type;
                return mask;
            }
        }
    }
}
