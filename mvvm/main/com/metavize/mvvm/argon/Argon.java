/*
 * Copyright (c) 2003, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id: Argon.java,v 1.33 2005/03/09 07:00:10 rbscott Exp $
 */

package com.metavize.mvvm.argon;

import java.net.InetAddress;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.metavize.jnetcap.Netcap;
import com.metavize.jnetcap.Shield;
import com.metavize.jnetcap.Range;
import com.metavize.jnetcap.SubscriptionGenerator;
import com.metavize.jnetcap.SubscriptionManager;
import com.metavize.jvector.Vector;

public class Argon
{
    /* Number of times to try and shutdown all vectoring machines cleanly before giving up */
    protected static final int SHUTDOWN_ATTEMPTS = 5;

    /* Amount of time between subsequent calls to shutdown all of the vectoring machines */
    protected static final int SHUTDOWN_PAUSE    = 2000;

    /* Separator inside of properties, cannot use space */
    protected static final String LIST_SEPARATOR = ",";

    /* Whether or not to subscribe to local traffic */
    protected static boolean ifLocal = false;

    /* Subscription manager */
    protected static final SubscriptionManager subManager = new SubscriptionManager();

    protected static int netcapDebugLevel    = 1;
    protected static int jnetcapDebugLevel   = 1;
    protected static int vectorDebugLevel    = 0;
    protected static int jvectorDebugLevel   = 0;
    protected static int mvutilDebugLevel    = 0;
    protected static boolean isShieldEnabled = true;
    protected static String shieldFile = null;
    protected static Shield shield;
    protected static String hostAntisubscribes = null;
    protected static String portAntisubscribes = null;
    protected static boolean udpAntisubscribe  = false;
    protected static boolean tcpAntisubscribe  = false;
    protected static boolean dhcpAntisubscribe = true;

    protected static String guardInside = null;
    
    /* By default block outside web, webs and postgres( just in case )*/
    protected static String guardOutside = null;

    /* By default, no IP regulated guards, an IP regulated guard takes the form
     * <port>[,<port>]*:<addr>[/<subnet as ip>], this syntax allows for multiple
     * guards to be separated by semicolons, if the port appears in both the
     * outside and regulated outside, the regulated outside rules are followed.
     * EG to allow access to HTTPs, HTTP and SSH to certain IPs
     * 22:10.0.0.43/255.255.255.0;443,80:1.2.3.4
     */
    protected static List<String[]> guardRegulatedOutside = new LinkedList<String[]>();

    /* Number of threads to donate to netcap */
    protected static int numThreads        = 10;

    /* Debugging */
    private static final Logger logger = Logger.getLogger( Argon.class );

    /* Inside device */
    protected static String inside  = "eth1";
    protected static String outside = "eth0";
    protected static String dmz[]   = null;

    public static void main( String args[] )
    {
        /* Get an instance of the shield */
        shield = Shield.getInstance();

        /* Parse all of the properties */
        parseProperties();

        init();

        registerHooks();

        subscribe( ifLocal );

        /* Wait for shutdown */
    }

    /**
     * Parse the user supplied properties
     */
    private static void parseProperties()
    {
        String temp;
        if (( temp = System.getProperty( "argon.inside" )) != null ) {
            inside = temp;
        }

        if (( temp = System.getProperty( "argon.outside" )) != null ) {
            outside = temp;
        }

        if (( temp = System.getProperty( "argon.numthreads" )) != null ) {
            int count;
            count = Integer.parseInt( temp );
            if ( count < 0 ) {
                throw new IllegalArgumentException( "argon.numthreads must be > 0. " + count ) ;
            }
            numThreads = count;
        }

        if (( temp = System.getProperty( "argon.debug.netcap" )) != null ) {
            netcapDebugLevel = Integer.parseInt( temp );
        }

        if (( temp = System.getProperty( "argon.debug.jnetcap" )) != null ) {
            jnetcapDebugLevel = Integer.parseInt( temp );
        }

        if (( temp = System.getProperty( "argon.debug.vector" )) != null ) {
            vectorDebugLevel = Integer.parseInt( temp );
        }

        if (( temp = System.getProperty( "argon.debug.jvector" )) != null ) {
            jvectorDebugLevel = Integer.parseInt( temp );
        }

        if (( temp = System.getProperty( "argon.debug.mvutil" )) != null ) {
            mvutilDebugLevel = Integer.parseInt( temp );
        }

        if (( temp = System.getProperty( "argon.shield.enabled" )) != null ) {
            isShieldEnabled = Boolean.parseBoolean( temp );
        }

        if (( temp = System.getProperty( "argon.shield.cfg_file" )) != null ) {
            shieldFile = temp;
        }

        if (( temp = System.getProperty( "argon.antisub.host" )) != null ) {
            hostAntisubscribes = temp;
        }

        if (( temp = System.getProperty( "argon.antisub.port" )) != null ) {
            portAntisubscribes = temp;
        }

        if (( temp = System.getProperty( "argon.antisub.tcp" )) != null ) {
            tcpAntisubscribe = Boolean.parseBoolean( temp );
        }

        if (( temp = System.getProperty( "argon.antisub.udp" )) != null ) {
            udpAntisubscribe = Boolean.parseBoolean( temp );
        }

        if (( temp = System.getProperty( "argon.antisub.dhcp" )) != null ) {
            dhcpAntisubscribe = Boolean.parseBoolean( temp );
        }

        if ((( temp = System.getProperty( "argon.guard.outside" )) != null ) && 
            !temp.trim().equals( "" )) {
            guardOutside = temp.trim();
        }

        if ((( temp = System.getProperty( "argon.guard.outside.regulated" )) != null ) &&
            !temp.trim().equals( "" )) {
            parseRegulatedGuard( temp );
        }

        if ((( temp = System.getProperty( "argon.guard.inside" )) != null ) &&
            !temp.trim().equals( "" )) {
            guardInside = temp.trim();
        }
    }

    private static void parseRegulatedGuard( String guards )
    {
        /* Split up all of the individual guards */
        String tmp[] = guards.split( ";" );
        
        
        for ( int c = 0 ; c < tmp.length ; c++ ) {
            String portAndHost[] = tmp[c].split( ":" );
            String[] s;
            switch( portAndHost.length ) {
            case 1:
                logger.warn( "Regulated guard without a host: " + tmp[c] );
                s = new String[2];
                s[0] = portAndHost[0];
                s[1] = null;
                break;
            case 2:
                s = portAndHost;                
                break;
            default:
                throw new IllegalArgumentException( "Invalid regulated guard string: " + tmp[c] );
            }

            /* insert the new regulated guard */
            guardRegulatedOutside.add( s );
        }
    }

    /**
     * Subscribe to all of the traffic going through the machine.<p/>
     *
     * @param local If true, subscribe to local traffic as well
     */
    private static void subscribe( boolean local )
    {
        int flags = SubscriptionGenerator.DEFAULT_FLAGS;

        /* Antisubscribe to traffic on local host */
        SubscriptionGenerator gen =
            new SubscriptionGenerator( SubscriptionGenerator.PROTOCOL_ALL, 
                                       flags | SubscriptionGenerator.ANTI_SUBSCRIBE );

        try {
            InetAddress localHost = InetAddress.getByAddress( new byte[] { 127, 0, 0, 0 } );
            InetAddress localMask = InetAddress.getByAddress( new byte[] { (byte)0xFF, 0, 0, 0 } );

            gen.server().address( localHost );
            gen.server().netmask( localMask );
            gen.client().address( localHost );
            gen.client().netmask( localMask );
        } catch ( Exception e ) {
            /* No way */
            throw new IllegalStateException( "Cannot create localhost" + e );
        }

        subManager.add( gen.subscribe());

        gen = new SubscriptionGenerator( SubscriptionGenerator.PROTOCOL_ALL, 
                                         flags | SubscriptionGenerator.ANTI_SUBSCRIBE );


        try {
            InetAddress multicastHost = InetAddress.getByAddress( new byte[] { (byte)0xE0, 0, 0, 0 } );
            InetAddress multicastMask = InetAddress.getByAddress( new byte[] { (byte)0xF0, 0, 0, 0 } );
            
            gen.server().address( multicastHost );
            gen.server().netmask( multicastMask );
        } catch ( Exception e ) {
            /* No way */
            throw new IllegalStateException( "Cannot create multicast" + e );
        }
        
        subManager.add( gen.subscribe());
        
        if ( local ) flags |= SubscriptionGenerator.LOCAL_ANTI_SUBSCRIBE;

        gen = new SubscriptionGenerator( Netcap.IPPROTO_TCP, flags );
        
        /* Subscribe to everything on TCP */
        subManager.add( gen.subscribe());

        /* Subscribe to everything on UDP */
        gen.protocol( Netcap.IPPROTO_UDP );
        subManager.add( gen.subscribe());

        /* Do all of the host anti-subscribes */
        antisubscribes();
    }

    private static void antisubscribes()
    {
        int flags = SubscriptionGenerator.DEFAULT_FLAGS | SubscriptionGenerator.ANTI_SUBSCRIBE;
                
        if ( tcpAntisubscribe ) {
            SubscriptionGenerator gen = new SubscriptionGenerator( Netcap.IPPROTO_TCP, flags );
            subManager.add( gen.subscribe());
        }

        if ( udpAntisubscribe ) {
            SubscriptionGenerator gen = new SubscriptionGenerator( Netcap.IPPROTO_UDP, flags );
            subManager.add( gen.subscribe());
        }

        if ( hostAntisubscribes != null ) {
            String hosts[] = hostAntisubscribes.split( LIST_SEPARATOR );
            SubscriptionGenerator gen = 
                new SubscriptionGenerator( SubscriptionGenerator.PROTOCOL_ALL, flags );
        
            for ( int c = 0 ; c < hosts.length ; c++ ) {
                try {
                    InetAddress host = InetAddress.getByName( hosts[c] );
                    
                    /* Set the address */
                    gen.server().address( host );
                    subManager.add( gen.subscribe());
                } catch( Exception e ) {
                    /* It could happen */
                    throw new IllegalArgumentException( "Antisubscribe host: " + hosts[c] + "\n" + e );
                }
            }
        }
        
        if ( portAntisubscribes != null ) {
            String ports[] = portAntisubscribes.split( LIST_SEPARATOR );
            SubscriptionGenerator gen =
                new SubscriptionGenerator( SubscriptionGenerator.PROTOCOL_ALL, flags );
        
            for ( int c = 0 ; c < ports.length ; c++ ) {
                int port = Integer.parseInt( ports[c] );
                if ( port < 0 || port > 0x65535 )
                    throw new IllegalArgumentException( "Antisubscribe port: " + ports[c] );
                
                /* Set the TCP port */
                gen.protocol( Netcap.IPPROTO_TCP );
                gen.server().port( port );
                subManager.add( gen.subscribe());

                /* Set the UDP port */
                gen.protocol( Netcap.IPPROTO_UDP );
                subManager.add( gen.subscribe());
            }
        }

        if ( dhcpAntisubscribe ) {
            /* If necessary antisubscribe on DHCP */
            SubscriptionGenerator gen =  new SubscriptionGenerator( Netcap.IPPROTO_UDP, flags );
            gen.server().port( new Range( 67, 68 ));
            subManager.add( gen.subscribe());
        }
    }

    /**
     * Register the TCP and UDP hooks
     */
    private static void registerHooks()
    {
        Netcap.registerUDPHook( UDPHook.getInstance());

        Netcap.registerTCPHook( TCPHook.getInstance());
    }

    /**
     * Initialize Netcap and any other supporting libraries.
     */
    private static void init ()
    {
        Netcap.init( isShieldEnabled, netcapDebugLevel, jnetcapDebugLevel );
        
        Vector.mvutilDebugLevel( mvutilDebugLevel );
        Vector.vectorDebugLevel( vectorDebugLevel );
        Vector.jvectorDebugLevel( jvectorDebugLevel );

        /* Donate a few threads */
        Netcap.donateThreads( numThreads );

        /* Start the scheduler */
        Netcap.startScheduler();

        /* Convert all of the interface names from strings to bytes */
        IntfConverter.init( inside, outside, dmz );

        guardsInsert();

        if ( isShieldEnabled && shieldFile != null )
            shield.config( shieldFile );


        /* Need the HUP thread: These will be controlled by requests of some sort. */

        /* Need the USR1 thread: These will be controlled by requests of some sort. */
    }

    public static void destroy() 
    {
        logger.debug( "Shutting down" );
        
        /* Remove both of the hooks to guarantee that no new sessions are created */
        Netcap.unregisterTCPHook();
        Netcap.unregisterUDPHook();

        VectronTable activeVectrons = VectronTable.getInstance();

        /* Close all of the vectoring machines */
        for ( int c = 0; c <  SHUTDOWN_ATTEMPTS ; c++ ) {
            if ( logger.isInfoEnabled()) {
                logger.info( "" + activeVectrons.count() + " active sessions remaining" );
            }
            
            if ( !activeVectrons.shutdownActive()) break;

            /* Sleep a little while vectrons shutdown. */
            try {
                Thread.sleep( SHUTDOWN_PAUSE );
            } catch ( InterruptedException e ) {
                logger.error( e.getMessage());
            }
        }

        /* Remove all of the subscriptions */
        subManager.unsubscribeAll();

        /* Remove all of the guards */
        guardsRemove();

        Netcap.cleanup();
    }

    private static void guardsInsert()
    {
        try {
            if ( guardInside != null && !guardInside.equals( "" ))
                Netcap.stationTcpGuard( IntfConverter.toNetcap( IntfConverter.INSIDE ), guardInside, null );
            
            if ( guardOutside != null && !guardOutside.equals( "" ))
                Netcap.stationTcpGuard( IntfConverter.toNetcap( IntfConverter.OUTSIDE ), guardOutside, null );

            for ( Iterator<String[]> iter = guardRegulatedOutside.iterator() ; iter.hasNext(); ) {
                String[] guard = iter.next();
                Netcap.stationTcpGuard( IntfConverter.toNetcap( IntfConverter.OUTSIDE ), guard[0], guard[1] );
            }

        } catch ( Exception e ) {
            logger.error( "Unable to relieve the guard on outside or inside ports", e );
        }
    }

    private static void guardsRemove()
    {
        /* Relieve the guard at port 80 on the outside */
        try {
            if ( guardInside != null )
                Netcap.relieveTcpGuard( IntfConverter.toNetcap( IntfConverter.INSIDE ), guardInside, null );
            
            if ( guardOutside != null )
                Netcap.relieveTcpGuard( IntfConverter.toNetcap( IntfConverter.OUTSIDE ), guardOutside, null );

            for ( Iterator<String[]> iter = guardRegulatedOutside.iterator() ; iter.hasNext(); ) {
                String[] guard = iter.next();
                Netcap.relieveTcpGuard( IntfConverter.toNetcap( IntfConverter.OUTSIDE ), guard[0], guard[1] );
            }
        } catch ( Exception e ) {
            logger.error( "Unable to relieve the guard on outside or inside ports", e );
        }
    }
}
