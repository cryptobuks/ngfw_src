/**
 * $Id$
 */
package com.untangle.node.ips;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import com.untangle.uvm.node.PortRange;
import com.untangle.uvm.node.SessionTuple;
import com.untangle.uvm.node.IPMatcher;
import com.untangle.uvm.vnet.Protocol;

public class IpsRuleHeader
{
    private static final Logger logger = Logger.getLogger( IpsRuleHeader.class );

    private static final Map<IpsRuleHeader, WeakReference<IpsRuleHeader>> INSTANCES = new WeakHashMap<IpsRuleHeader, WeakReference<IpsRuleHeader>>();

    private final int action;
    private final Protocol protocol;

    private final Set<IPMatcher> clientIpSet;
    private final PortRange clientPortRange;

    private final boolean bidirectional;
    public static enum Direction { BOTH, INBOUND, OUTBOUND };
    private final Direction direction;
    
    private final Set<IPMatcher> serverIpSet;
    private final PortRange serverPortRange;

    private final boolean clientIPFlag;
    private final boolean clientPortFlag;
    private final boolean serverIPFlag;
    private final boolean serverPortFlag;

    // constructors ------------------------------------------------------------

    private IpsRuleHeader(int action, boolean bidirectional, Direction direction,
                          Protocol protocol,
                          List<IPMatcher> clientIPList,
                          PortRange clientPortRange,
                          List<IPMatcher> serverIPList,
                          PortRange serverPortRange,
                          boolean clientIPFlag, boolean clientPortFlag,
                          boolean serverIPFlag, boolean serverPortFlag)
    {

        this.action = action;
        this.bidirectional = bidirectional;
        this.direction = direction;
        this.protocol = protocol;
                                       
        if (clientIPList == null)
            this.clientIpSet = new HashSet<IPMatcher>();
        else
            this.clientIpSet = new HashSet<IPMatcher>(clientIPList);
        if (serverIPList == null)
            this.serverIpSet = new HashSet<IPMatcher>();
        else
            this.serverIpSet = new HashSet<IPMatcher>(serverIPList);
        
        this.clientPortRange = clientPortRange;
        this.serverPortRange = serverPortRange;

        this.clientIPFlag = clientIPFlag;
        this.clientPortFlag = clientPortFlag;
        this.serverIPFlag = serverIPFlag;
        this.serverPortFlag = serverPortFlag;
    }

    // static methods ----------------------------------------------------------

    public static IpsRuleHeader getHeader(int action, boolean bidirectional, Direction direction,
                                          Protocol protocol,
                                          List<IPMatcher> clientIPList,
                                          PortRange clientPortRange,
                                          List<IPMatcher> serverIPList,
                                          PortRange serverPortRange,
                                          boolean clientIPFlag,
                                          boolean clientPortFlag,
                                          boolean serverIPFlag,
                                          boolean serverPortFlag)

    {
        IpsRuleHeader h = new IpsRuleHeader(action, bidirectional, direction, protocol, 
                                            clientIPList, clientPortRange,
                                            serverIPList,serverPortRange,
                                            clientIPFlag, clientPortFlag,
                                            serverIPFlag, serverPortFlag);

        synchronized (INSTANCES) {
            WeakReference<IpsRuleHeader> wr = INSTANCES.get(h);
            if (null != wr) {
                IpsRuleHeader c = wr.get();
                if (null != c) {
                    h = c;
                } else {
                    INSTANCES.put(h, new WeakReference<IpsRuleHeader>(h));
                }
            } else {
                INSTANCES.put(h, new WeakReference<IpsRuleHeader>(h));
            }
        }

        return h;
    }

    // public methods ----------------------------------------------------------

    public boolean portMatches( int port, boolean toServer )
    {
        if(toServer)
            return serverPortFlag ^ serverPortRange.contains(port);
        else
            return clientPortFlag ^ clientPortRange.contains(port);
    }

    public boolean matches( SessionTuple sess, boolean sessInbound, boolean forward )
    {
        return matches(sess, sessInbound, forward, false);
    }

    private boolean matches( SessionTuple sess, boolean sessInbound, boolean forward, boolean swapFlag )
    {
        if(this.protocol != Protocol.getInstance(sess.getProtocol()))
            return false;

        /**Check Port Match*/
        boolean clientPortMatch = clientPortRange.contains(forward ? sess.getClientPort() : sess.getServerPort());
        boolean serverPortMatch = serverPortRange.contains(forward ? sess.getServerPort() : sess.getClientPort());
        boolean portMatch = (clientPortMatch ^ clientPortFlag) && (serverPortMatch ^ serverPortFlag);

        if(!portMatch && !bidirectional)
            return false;

        if ( this.direction == Direction.OUTBOUND && sessInbound )
            return false;
        if ( this.direction == Direction.INBOUND && !sessInbound )
            return false;
        
        boolean isInbound = forward ? sessInbound : !sessInbound;

        /**Check IP Match*/
        InetAddress cAddr = forward ? sess.getClientAddr() : sess.getServerAddr();
        boolean clientIPMatch = false;
        Iterator<IPMatcher> clientIt = clientIpSet.iterator();

        while(clientIt.hasNext() && !clientIPMatch) {
            IPMatcher matcher = clientIt.next();
            clientIPMatch = matcher.isMatch(cAddr);
            // logger.debug("client matcher: " + matcher + " sez: " + clientIPMatch);
        }

        InetAddress sAddr = forward ? sess.getServerAddr() : sess.getClientAddr();
        boolean serverIPMatch = false;
        Iterator<IPMatcher> serverIt = serverIpSet.iterator();
        while(serverIt.hasNext() && !serverIPMatch) {
            IPMatcher matcher = serverIt.next();
            serverIPMatch = matcher.isMatch(sAddr);
            // logger.debug("server matcher: " + matcher + " sez: " + serverIPMatch);
        }
        boolean ipMatch = (clientIPMatch ^ clientIPFlag) && (serverIPMatch ^ serverIPFlag);

        /**Check Directional flag*/
        if(!(ipMatch && portMatch) && bidirectional && !swapFlag) {
            return matches(sess, sessInbound, !forward, true);
        }

        return ipMatch && portMatch;
    }

    public int getAction()
    {
        return action;
    }

    // Rule manager uses this to decide if the rule is already
    // known.
    public boolean matches(IpsRuleHeader other)
    {
        boolean action = (this.action == other.action);
        boolean protocol = (this.protocol == other.protocol); // ?
        boolean clientPorts = (this.clientPortRange.equals(other.clientPortRange));
        boolean serverPorts = (this.serverPortRange.equals(other.serverPortRange));
        boolean serverIP = (this.serverIpSet.equals(other.serverIpSet));
        boolean clientIP = (this.serverIpSet.equals(other.serverIpSet));
        boolean direction = (this.bidirectional == other.bidirectional);

        return action && protocol && clientPorts && serverPorts && serverIP && clientIP && direction;
    }

    // Object methods ----------------------------------------------------------

    public String toString()
    {
        String str = "alert "+protocol+" ";
        if(clientIPFlag)
            str += "!";
        str += clientIpSet + " ";
        if(clientPortFlag)
            str += "!";
        str += clientPortRange;
        if(bidirectional)
            str += " <> ";
        else
            str += " -> ";
        if(serverIPFlag)
            str += "!";
        str += serverIpSet +" ";
        if(serverPortFlag)
            str += "!";
        str += serverPortRange;
        return str;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof IpsRuleHeader)) {
            return false;
        }

        IpsRuleHeader h = (IpsRuleHeader)o;

        return action == h.action
            && protocol.equals(h.protocol)
            && clientIpSet.equals(h.clientIpSet)
            && clientPortRange.equals(h.clientPortRange)
            && bidirectional == h.bidirectional
            && serverIpSet.equals(h.serverIpSet)
            && serverPortRange.equals(h.serverPortRange)
            && clientIPFlag == h.clientIPFlag
            && clientPortFlag == h.clientPortFlag
            && serverIPFlag == h.serverIPFlag
            && serverPortFlag == h.serverIPFlag;
    }

    public int hashCode()
    {
        int result = 17;
        result = 37 * result + action;
        result = 37 * result + protocol.hashCode();
        result = 37 * result + clientIpSet.hashCode();
        result = 37 * result + clientPortRange.hashCode();
        result = 37 * result + (bidirectional ? 1 : 0);
        result = 37 * result + serverIpSet.hashCode();
        result = 37 * result + serverPortRange.hashCode();
        result = 37 * result + (clientIPFlag ? 1 : 0);
        result = 37 * result + (clientPortFlag ? 1 : 0);
        result = 37 * result + (serverIPFlag ? 1 : 0);
        result = 38 * result + (serverPortFlag ? 1 : 0);

        return result;
    }
}
