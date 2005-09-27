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

package com.metavize.mvvm.engine;

import java.util.List;
import java.util.LinkedList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.Inet4Address;

import com.metavize.jnetcap.Netcap;
import com.metavize.jnetcap.InterfaceData;

import com.metavize.mvvm.ArgonManager;
import com.metavize.mvvm.IntfEnum;
import com.metavize.mvvm.MvvmContextFactory;
import com.metavize.mvvm.MvvmException;
import com.metavize.mvvm.NetworkingConfiguration;
import com.metavize.mvvm.NetworkingManager;
import com.metavize.mvvm.ArgonManager;
import com.metavize.mvvm.MvvmException;
import com.metavize.mvvm.InterfaceAlias;
import com.metavize.mvvm.argon.ArgonManagerImpl;
import com.metavize.mvvm.InterfaceAlias;
import com.metavize.mvvm.tran.IPaddr;
import org.apache.log4j.Logger;

class NetworkingManagerImpl implements NetworkingManager
{
    // These are the predefined ones.  There can be others.
    private static final byte   INTF_EXTERNAL_NUM = 0;
    private static final String INTF_EXTERNAL_NAME = "External";
    private static final byte   INTF_INTERNAL_NUM = 1;
    private static final String INTF_INTERNAL_NAME = "Internal";
    private static final byte   INTF_DMZ_NUM = 2;
    private static final String INTF_DMZ_NAME = "DMZ";

    private static final String HEADER         = "##AUTOGENERATED BY METAVIZE DO NOT MODIFY MANUALLY\n\n";
    private static final String BUNNICULA_BASE = System.getProperty( "bunnicula.home" );
    private static final String BUNNICULA_CONF = System.getProperty( "bunnicula.conf.dir" );

    private static final String BUNNICULA_RESET_SCRIPT = BUNNICULA_BASE + "/mvvm_restart.sh";
    private static final String SSH_ENABLE_SCRIPT      = BUNNICULA_BASE + "/ssh_enable.sh";
    private static final String SSH_DISABLE_SCRIPT     = BUNNICULA_BASE + "/ssh_disable.sh";
    private static final String DHCP_RENEW_SCRIPT      = BUNNICULA_BASE + "/networking/dhcp-renew";

    private static final String IP_CFG_FILE    = "/etc/network/interfaces";
    private static final String NS_CFG_FILE    = "/etc/resolv.conf";
    private static final String FLAGS_CFG_FILE = "/networking.sh";
    private static final String SSHD_PID_FILE  = "/var/run/sshd.pid";

    private static final String NS_PARAM       = "nameserver";

    private static final String FLAG_TCP_WIN   = "TCP_WINDOW_SCALING_EN";
    private static final String FLAG_HTTP_IN   = "MVVM_ALLOW_IN_HTTP";
    private static final String FLAG_HTTPS_OUT = "MVVM_ALLOW_OUT_HTTPS";
    private static final String FLAG_HTTPS_RES = "MVVM_ALLOW_OUT_RES";
    private static final String FLAG_OUT_NET   = "MVVM_ALLOW_OUT_NET";
    private static final String FLAG_OUT_MASK  = "MVVM_ALLOW_OUT_MASK";
    private static final String FLAG_EXCEPTION = "MVVM_IS_EXCEPTION_REPORTING_EN";

    private static final Logger logger = Logger.getLogger( NetworkingManagerImpl.class );

    private static NetworkingManagerImpl INSTANCE = new NetworkingManagerImpl();

    /* A cache of the current configuration */
    NetworkingConfiguration configuration = new NetworkingConfiguration();

    private IntfEnum intfEnum;

    /**
     * Retrieve the current network configuration
     */
    public synchronized NetworkingConfiguration get()
    {
        /* Retrieve all of the networking parameters */
        refresh();

        /* Return a copy of the current configuration */
        /* XXX Need to make a copy */
        return configuration;
    }

    /**
     * Set a network configuration.
     * @param configuration - Configuration to save
     */
    public synchronized void set( NetworkingConfiguration netConfig )
    {
        this.configuration = netConfig;

        save();

        try {
            MvvmContextFactory.context().argonManager().loadNetworkingConfiguration( netConfig );
        } catch ( Exception ex ) {
            logger.error( "Unable to load networking configuration", ex );
        }
    }

    private NetworkingManagerImpl()
    {
    }

    private void refresh()
    {
        /* Create a new network configuration with all defaults */
        configuration = new NetworkingConfiguration();

        /* Retrieve the DHCP configuration */
        getInterface();
        buildIntfEnum();
        getNameservers();
        getFlags();
        getSsh();
    }

    private void getInterface()
    {
        getDhcp();

        ArgonManager argon = ArgonManagerImpl.getInstance();

        configuration.host( new IPaddr((Inet4Address)argon.getOutsideAddress()));
        configuration.netmask( new IPaddr((Inet4Address)argon.getOutsideNetmask()));
        configuration.gateway( new IPaddr((Inet4Address)Netcap.getGateway()));

        List<InterfaceAlias> list = new LinkedList<InterfaceAlias>();
        /* XXX Should be exposed in the manager, but the the InterfaceData from jnetcap has
         * to be exposed */
        for ( InterfaceData data : ((ArgonManagerImpl)argon).getOutsideAliasList()) {
            list.add( new InterfaceAlias( data.getAddress(), data.getNetmask(), data.getBroadcast()));
        }
        configuration.setAliasList( list );
    }

    private void getDhcp()
    {
        BufferedReader in = null;

        /* Open up the interfaces file */
        try {
            in = new BufferedReader(new FileReader( IP_CFG_FILE ));
            String str;
            while ((str = in.readLine()) != null) {
                str = str.trim();
                if ( str.startsWith( "iface br0" )) {
                    if ( str.contains( "dhcp" )) {
                        configuration.isDhcpEnabled( true );
                    }
                }
            }
        } catch ( Exception ex ) {
            logger.error( "Error reading file: ", ex );
        }

        close( in );
    }

    private void getNameservers()
    {
        BufferedReader in = null;

        /* Open up the interfaces file */
        try {
            in = new BufferedReader(new FileReader( NS_CFG_FILE ));
            String str;
            while ((str = in.readLine()) != null) {
                str = str.trim();
                if ( str.startsWith( NS_PARAM )) {
                    IPaddr dns = IPaddr.parse( str.substring( NS_PARAM.length() ));

                    if ( configuration.dns1().isEmpty()) {
                        configuration.dns1( dns );
                    } else {
                        configuration.dns2( dns );
                        break;
                    }
                }
            }
        } catch ( Exception ex ) {
            logger.error( "Error reading file: ", ex );
        }

        close( in );
    }

    private void getFlags()
    {
        String host = null;
        String mask = null;

        /* Open up the interfaces file */
        try {
            BufferedReader in = new BufferedReader(new FileReader( BUNNICULA_CONF + FLAGS_CFG_FILE ));
            String str;
            while ((str = in.readLine()) != null) {
                str = str.trim();
                try {
                    if ( str.startsWith( "#" )) {
                        continue;
                    } else if ( str.startsWith( FLAG_TCP_WIN )) {
                        configuration.isTcpWindowScalingEnabled( parseBooleanFlag( str, FLAG_TCP_WIN ));
                    } else if ( str.startsWith( FLAG_HTTP_IN )) {
                        configuration.isInsideInsecureEnabled( parseBooleanFlag( str, FLAG_HTTP_IN ));
                    } else if ( str.startsWith( FLAG_HTTPS_OUT )) {
                        configuration.isOutsideAccessEnabled( parseBooleanFlag( str, FLAG_HTTPS_OUT ));
                    } else if ( str.startsWith( FLAG_HTTPS_RES )) {
                        configuration.isOutsideAccessRestricted( parseBooleanFlag( str, FLAG_HTTPS_RES ));
                    } else if ( str.startsWith( FLAG_OUT_NET )) {
                        host = str.substring( FLAG_OUT_NET.length() + 1 );
                    } else if ( str.startsWith( FLAG_OUT_MASK )) {
                        mask = str.substring( FLAG_OUT_MASK.length() + 1 );
                    } else if ( str.startsWith( FLAG_EXCEPTION )) {
                        configuration.isExceptionReportingEnabled( parseBooleanFlag( str, FLAG_EXCEPTION ));
                    }
                } catch ( Exception ex ) {
                    logger.warn( "Error while retrieving flags", ex );
                }
            }
            in.close();
        } catch ( FileNotFoundException ex ) {
            logger.warn( "Could not read '" + BUNNICULA_CONF + FLAGS_CFG_FILE +
                         "' because it doesn't exist" );
        } catch ( Exception ex ) {
            logger.warn( "Error reading file: ", ex );
        }


        try {
            if ( host != null ) {
                configuration.outsideNetwork( IPaddr.parse( host ));

                if ( mask != null ) {
                    configuration.outsideNetmask( IPaddr.parse( mask ));
                }
            }
        } catch ( Exception ex ) {
            logger.error( "Error parsing outside host or netmask", ex );
        }
    }

    private Boolean parseBooleanFlag( String nameValuePair, String name )
    {
        if ( nameValuePair.length() < name.length() + 1 )
            return null;

        nameValuePair = nameValuePair.substring( name.length() + 1 );
        return Boolean.parseBoolean( nameValuePair );
    }

    private void getSsh()
    {
        /* SSH is enabled if and only if this file exists */
        File sshd = new File( SSHD_PID_FILE );

        configuration.isSshEnabled( sshd.exists());
    }

    private void save()
    {
        saveInterfaces();
        saveNameservers();
        saveFlags();
        saveSsh();
    }

    private void saveInterfaces()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( HEADER );
        sb.append( "# The loopback network interface\n" );
        sb.append( "auto lo\n" );
        sb.append( "auto br0\n" );
        sb.append( "iface lo inet loopback\n\n# The bridge interface\n" );

        if ( configuration.isDhcpEnabled()) {
            sb.append( "iface br0 inet dhcp\n" );
        } else {
            sb.append( "iface br0 inet static\n" );
            sb.append( "\taddress " + configuration.host()    + "\n" );
            sb.append( "\tnetmask " + configuration.netmask() + "\n" );
            sb.append( "\tgateway " + configuration.gateway() + "\n" );
        }

        sb.append( "\tbridge_ports all\n" );
        sb.append( "\tbridge_maxwait 0\n" );

        int c = 0;
        for ( InterfaceAlias alias : configuration.getAliasList()) {
            if ( !alias.isValid()) {
                logger.warn( "Ignoring an invalid alias" );
                continue;
            }
            String aliasName = "br0:" + c++;
            
            /* Indicate that the alias should be configured at startup */
            sb.append( "\nauto " + aliasName + "\n" );
            
            /* Write out the alias configuration */
            sb.append( "iface " + aliasName + " inet static\n" );
            sb.append( "\taddress " + alias.getAddress() + "\n" );
            sb.append( "\tnetmask " + alias.getNetmask() + "\n" );
            sb.append( "\n\n" );
        }
        
        writeFile( sb, IP_CFG_FILE );
        writeFile( sb, BUNNICULA_CONF + IP_CFG_FILE );
    }


    private void saveNameservers() {
        StringBuilder sb = new StringBuilder();

        if ( configuration.isDhcpEnabled())
            return;

        sb.append( HEADER );
        sb.append( NS_PARAM + " " + configuration.dns1() + "\n" );

        if ( !configuration.dns2().isEmpty())
            sb.append( NS_PARAM + " " + configuration.dns2() + "\n" );

        /* XXX write both files */
        writeFile( sb, NS_CFG_FILE );
        writeFile( sb, BUNNICULA_CONF + NS_CFG_FILE );
    }

    private void saveFlags() {
        StringBuilder sb = new StringBuilder();

        sb.append( "#!/bin/sh\n" );
        sb.append( HEADER + "\n" );
        sb.append( "## Set to true to enable\n" );
        sb.append( "## false or undefined is disabled.\n" );
        sb.append( FLAG_TCP_WIN + "=" + configuration.isTcpWindowScalingEnabled() + "\n\n" );
        sb.append( "## Allow inside HTTP true to enable\n" );
        sb.append( "## false or undefined is disabled.\n" );
        sb.append( FLAG_HTTP_IN + "=" + configuration.isInsideInsecureEnabled() + "\n\n" );
        sb.append( "## Allow outside HTTPS true to enable\n" );
        sb.append( "## false or undefined to disable.\n" );
        sb.append( FLAG_HTTPS_OUT + "=" + configuration.isOutsideAccessEnabled() + "\n\n" );
        sb.append( "## Restrict outside HTTPS access\n" );
        sb.append( "## True if restricted, undefined or false if unrestricted\n" );
        sb.append( FLAG_HTTPS_RES + "=" + configuration.isOutsideAccessRestricted() + "\n\n" );
        sb.append( "## Report exceptions\n" );
        sb.append( "## True to send out exception logs, undefined or false for not\n" );
        sb.append( FLAG_EXCEPTION + "=" + configuration.isExceptionReportingEnabled() + "\n\n" );


        if ( !configuration.outsideNetwork().isEmpty()) {
            IPaddr network = configuration.outsideNetwork();
            IPaddr netmask = configuration.outsideNetmask();

            sb.append( "## If outside access is enabled and restricted, only allow access from\n" );
            sb.append( "## this network.\n" );

            sb.append( FLAG_OUT_NET + "=" + network + "\n" );

            if ( !netmask.isEmpty()) {
                sb.append( FLAG_OUT_MASK + "=" + netmask + "\n" );
            }
            sb.append( "\n" );
        }

        writeFile( sb, BUNNICULA_CONF + FLAGS_CFG_FILE );
    }

    private void saveSsh()
    {
        try {
            if ( configuration.isSshEnabled()) {
                Runtime.getRuntime().exec( SSH_ENABLE_SCRIPT );
            } else {
                Runtime.getRuntime().exec( SSH_DISABLE_SCRIPT );
            }
        } catch ( Exception ex ) {
            logger.error( "Unable to configure ssh", ex );
        }
    }

    private void writeFile( StringBuilder sb, String fileName )
    {
        BufferedWriter out = null;

        /* Open up the interfaces file */
        try {
            String data = sb.toString();

            out = new BufferedWriter(new FileWriter( fileName ));
            out.write( data, 0, data.length());
        } catch ( Exception ex ) {
            /* XXX May need to catch this exception, restore defaults
             * then try again */
            logger.error( "Error writing file " + fileName + ":", ex );
        }

        close( out );
    }

    static NetworkingManagerImpl getInstance()
    {
        return INSTANCE;
    }

    private void close( BufferedReader buf )
    {
        try {
            if ( buf != null )
                buf.close();
        } catch ( Exception ex ) {
            logger.error( "Unable to close file", ex );
        }
    }

    private void close( BufferedWriter buf )
    {
        try {
            if ( buf != null )
                buf.close();
        } catch ( Exception ex ) {
            logger.error( "Unable to close file", ex );
        }
    }

    public NetworkingConfiguration renewDhcpLease() throws Exception {
        /* Renew the address */
        Process p = Runtime.getRuntime().exec( "sh " + DHCP_RENEW_SCRIPT );

        if ( p.waitFor() != 0 ) {
            throw new MvvmException( "Error while Renewing DHCP Lease" );
        }

        /* Update the address and generate new rules */
        MvvmContextFactory.context().argonManager().updateAddress();

        /* Return a new copy of the Networking configuration */
        return get();
    }

    public IntfEnum getIntfEnum()
    {
        return intfEnum;
    }
    
    void buildIntfEnum()
    {
        ArgonManager argon = ArgonManagerImpl.getInstance();

        byte[] argonInterfaces = 
            // XXXX
            // argon.getInterfaces();
            new byte[] { INTF_EXTERNAL_NUM, INTF_INTERNAL_NUM };
        String[] intfNames =
            new String[] { INTF_EXTERNAL_NAME, INTF_INTERNAL_NAME };
        intfEnum = new IntfEnum(argonInterfaces, intfNames);
    }
        
}
