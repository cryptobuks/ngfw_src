/*
 * Copyright (c) 2003 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id: NetcapTCPSession.java,v 1.10 2005/01/17 21:12:10 rbscott Exp $
 */


package com.metavize.jnetcap;

import java.net.InetAddress;

public class NetcapTCPSession extends NetcapSession
{
    private static final int FLAG_FD                    = 32;
    private static final int FLAG_ACKED                 = 33;

    public  static final int NON_LOCAL_BIND             = 1;
    
    private static final int DEFAULT_SERVER_START_FLAGS = 0;
    private static final int DEFAULT_SERVER_COMPLETE_FLAGS = NON_LOCAL_BIND;
    private static final int DEFAULT_CLIENT_COMPLETE_FLAGS = 0;
    private static final int DEFAULT_RESET_FLAGS        = 0;
    
    public NetcapTCPSession( int id )
    {
        super( id, Netcap.IPPROTO_TCP );
    }
    
    /* Get whether or not the client has already ACKED the session */
    public boolean acked()
    {
        return ( getIntValue( FLAG_ACKED, pointer.value()) == 1 ) ? true : false;
    }

    /* ??? This is a dirty hack to work around the fact that you cannot overwrite *
     * the return value with a subclass, this is fixed in 1.5.0                   *
     * in 1.5, this would just read:
     * public TCPEndpoints clientSide() { return clientSide; }
     * public TCPEndpoints serverSide() { return serverSide; }
     * Perhaps this may be too confusing.
     */
    public TCPEndpoints tcpClientSide() { return (TCPEndpoints)clientSide; }
    public TCPEndpoints tcpServerSide() { return (TCPEndpoints)serverSide; }

    protected Endpoints makeEndpoints( boolean ifClient ) 
    {
        return new TCPSessionEndpoints( ifClient );
    }
    
    /**
     * Complete the connection to a client, this may throw an exception if
     * the connection is not completed succesfully.</p>
     * @param flags - Flags for the client complete.
     */
    public void clientComplete( int flags )
    {
        clientComplete( pointer.value(), flags );
    }

    /**
     * Complete the connection to a client with the default flags.
     * this will throw an exception if the connection is not completed 
     * succesfully.</p>
     * @param flags - Flags for the client complete.
     */
    public void clientComplete()
    {
        clientComplete( DEFAULT_CLIENT_COMPLETE_FLAGS );
    }

    /**
     * Reset the connection to the client.</p>
     * @param flags - Flags for the client reset.
     */
    public void clientReset( int flags )
    {
        clientReset( pointer.value(), flags );
    }

    /**
     * Reset the connection to the client with default flags.
     */
    public void clientReset()
    {
        clientReset( DEFAULT_RESET_FLAGS );
    }

    /* Start completing the connection to the server */
    public void serverStart( int flags )
    {
        serverStart( pointer.value(), flags );
    }

    public void serverStart()
    {
        serverStart( DEFAULT_SERVER_START_FLAGS );
    }
    
    /* Complete the connection, every other function should call this after setup */
    public void   serverComplete( int flags )
    {
        serverComplete( pointer.value(), flags );
    }

    public void   serverComplete()
    {
        serverComplete( DEFAULT_SERVER_COMPLETE_FLAGS );
    }

    /* Connect to the server with new settings on the traffic side */
    public void   serverComplete( InetAddress clientAddress, int clientPort,
                                  InetAddress serverAddress, int serverPort, int flags )
    {
        if ( clientAddress == null ) clientAddress = serverSide.client().host();
        if ( clientPort    == 0    ) clientPort    = serverSide.client().port();
        if ( serverAddress == null ) serverAddress = serverSide.server().host();
        if ( serverPort    == 0    ) serverPort    = serverSide.server().port();
        
        if ( setServerEndpoint( pointer.value(), Inet4AddressConverter.toLong( clientAddress ), clientPort,
                                Inet4AddressConverter.toLong( serverAddress ), serverPort ) < 0 ) {
            Netcap.error( "Unable to modify the server endpoint" + pointer.value());
        }

        /* XX If the destination is local, then you have to remap the connection, this
         * will be dealt with at a later date */
        
        serverComplete( flags );
    }
    
    public void   serverComplete( InetAddress clientAddress, int clientPort,
                                  InetAddress serverAddress, int serverPort ) 
    {
        serverComplete( clientAddress, clientPort, serverAddress, serverPort, DEFAULT_SERVER_COMPLETE_FLAGS );
    }
    
    private static native int setServerEndpoint( long sessionPointer, long clientAddress, int clientPort,
                                                 long serverAddress, int serverPort );
    private static native void clientComplete( long sessionPointer, int flags );
    private static native void clientReset( long sessionPointer, int flags );
    private static native void serverStart( long sessionPointer, int flags );
    private static native void serverComplete( long sessionPointer, int flags );

    private static native void close( long sessionPointer, boolean ifClient );
    
    private static native int read( long sessionPointer, boolean ifClient, byte[] data );
    private static native int write( long sessionPointer, boolean ifClient, byte[] data );

    /**
     * Set the blocking flag for one of the file descriptors.  This will throw an
     * error if it is unable to set the flag.
     */
    private static native void blocking( long sessionPointer, boolean ifClient, boolean mode );

    protected class TCPSessionEndpoints extends SessionEndpoints implements TCPEndpoints
    {
        public TCPSessionEndpoints( boolean ifClientSide )
        {
            super( ifClientSide );
        }
                
        public int fd() { return getIntValue( buildMask( FLAG_FD ), pointer.value()); }
        
        /**
         * Set the blocking flag for one of the file descriptors.  This will throw an
         * error if it is unable to set the flag.
         */
        public void blocking( boolean mode ) {
            NetcapTCPSession.blocking( pointer.value(), ifClientSide, mode );
        }

        public int read( byte[] data )
        {
            return NetcapTCPSession.read( pointer.value(), ifClientSide, data );
        }
        
        public int write( byte[] data )
        {
            return NetcapTCPSession.write( pointer.value(), ifClientSide, data );
        }

        public int write( String data )
        {
            return write( data.getBytes());
        }

        /**
         * Close a file descriptor associated with one of the sides.  This will
         * throw an error if it is unable to close the file desscriptor */
        public void close()
        {
            NetcapTCPSession.close( pointer.value(), ifClientSide );
        }
        
        public int buildMask( int type )
        {
            return ( ifClientSide ? FLAG_IF_CLIENT_MASK : 0 ) | type;
        }
    }

    static 
    {
        Netcap.load();
    }
}
