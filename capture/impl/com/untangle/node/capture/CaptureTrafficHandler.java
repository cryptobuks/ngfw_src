/**
 * $Id: CaptureTrafficHandler.java,v 1.00 2011/12/14 01:02:03 mahotz Exp $
 */

package com.untangle.node.capture;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.untangle.uvm.vnet.event.TCPNewSessionRequestEvent;
import com.untangle.uvm.vnet.event.TCPSessionEvent;
import com.untangle.uvm.vnet.event.UDPNewSessionRequestEvent;
import com.untangle.uvm.vnet.event.UDPSessionEvent;
import com.untangle.uvm.vnet.event.UDPPacketEvent;
import com.untangle.uvm.vnet.AbstractEventHandler;
import com.untangle.uvm.vnet.IPNewSessionRequest;
import com.untangle.uvm.vnet.NodeTCPSession;
import com.untangle.uvm.vnet.NodeUDPSession;
import com.untangle.uvm.UvmContextFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class CaptureTrafficHandler extends AbstractEventHandler
{
    private final Logger logger = Logger.getLogger(getClass());
    private CaptureNodeImpl node = null;

    public CaptureTrafficHandler(CaptureNodeImpl node)
    {
        super(node);
        this.node = node;
    }

///// TCP stuff --------------------------------------------------

    @Override
    public void handleTCPNewSessionRequest(TCPNewSessionRequestEvent event)
    {
        IPNewSessionRequest sessreq = event.sessionRequest();

        // first we look for and ignore all traffic on port 80 since
        // the http-casing handler will take care of all that
        if (sessreq.getNatToPort() == 80)
        {
            sessreq.release();
            return;
        }

        String clientAddr = sessreq.getClientAddr().getHostAddress().toString();
        String serverAddr = sessreq.getServerAddr().getHostAddress().toString();

        // next check is to see if the user is already authenticated
        // or the client or server is in one of the pass lists
        if (node.isSessionAllowed(clientAddr,serverAddr) == true)
        {
            node.incrementBlinger(CaptureNode.BlingerType.SESSALLOW,1);
            sessreq.release();
            return;
        }

        // not authenticated and no pass list match so check the rules
        CaptureRule rule = node.checkCaptureRules(sessreq);

        // by default we allow traffic so if there is no rule or we
        // find a pass rule then let the traffic continue here
        if ((rule == null) || (rule.getBlock() == false))
        {
            if (rule != null)
            {
                CaptureRuleEvent logevt = new CaptureRuleEvent(sessreq.sessionEvent(), rule);
                node.logEvent(logevt);
            }

            node.incrementBlinger(CaptureNode.BlingerType.SESSALLOW,1);
            sessreq.release();
            return;
        }

        // not yet allowed and we found a block rule so shut it down
        CaptureRuleEvent logevt = new CaptureRuleEvent(sessreq.sessionEvent(), rule);
        node.logEvent(logevt);
        node.incrementBlinger(CaptureNode.BlingerType.SESSBLOCK,1);
        sessreq.rejectSilently();
    }

///// UDP stuff --------------------------------------------------

    @Override
    public void handleUDPNewSessionRequest(UDPNewSessionRequestEvent event)
    {
        IPNewSessionRequest sessreq = event.sessionRequest();

        String clientAddr = sessreq.getClientAddr().getHostAddress().toString();
        String serverAddr = sessreq.getServerAddr().getHostAddress().toString();

        // first check is to see if the user is already authenticated
        // or the client or server is in one of the pass lists
        if (node.isSessionAllowed(clientAddr,serverAddr) == true)
        {
            node.incrementBlinger(CaptureNode.BlingerType.SESSALLOW,1);
            sessreq.release();
            return;
        }

        // not authenticated and no pass list match so check the rules
        CaptureRule rule = node.checkCaptureRules(sessreq);

        // by default we allow traffic so if there is no rule or we
        // find a pass rule then let the traffic continue here
        if ((rule == null) || (rule.getBlock() == false))
        {
            if (rule != null)
            {
                CaptureRuleEvent logevt = new CaptureRuleEvent(sessreq.sessionEvent(), rule);
                node.logEvent(logevt);
            }

            node.incrementBlinger(CaptureNode.BlingerType.SESSALLOW,1);
            sessreq.release();
            return;
        }

        // traffic not yet allowed so we hook DNS traffic which will
        // allow us to do the lookup ourselves.  this will ensure we
        // can't be circumvented by creative UDP port 53 traffic and it
        // will allow HTTP requests to become established which is
        // required for the http-casing to do the redirect
        if (sessreq.getServerPort() == 53)
        {
            // attach an empty for the packet handler to use
            DNSPacket packet = new DNSPacket();
            sessreq.attach(packet);
            return;
        }

        // not yet allowed and we found a block rule and the traffic
        // isn't DNS so shut it down
        CaptureRuleEvent logevt = new CaptureRuleEvent(sessreq.sessionEvent(), rule);
        node.logEvent(logevt);
        node.incrementBlinger(CaptureNode.BlingerType.SESSBLOCK,1);
        sessreq.rejectSilently();
    }

    @Override
    public void handleUDPClientPacket(UDPPacketEvent event)
    {
        NodeUDPSession session = event.session();
        DNSPacket packet = (DNSPacket)session.attachment();
        InetAddress addr = null;
        session.attach(null);

        // extract the DNS query from the client packet
        packet.ExtractQuery(event.data().array(),event.data().limit());
        logger.debug(packet.toString());

            // this handler will only see UDP packets with a target port
            // of 53 sent from unauthenticated client so if it doesn't seem
            // like a valid DNS query we just ignore and block
            if (packet.isValidDNSQuery() != true)
            {
                node.incrementBlinger(CaptureNode.BlingerType.SESSBLOCK,1);
                session.expireClient();
                session.expireServer();
                session.release();
            }

        // we have a valid query so lets lookup the address
        try
        {
            addr = InetAddress.getByName(packet.getQname());
        }

        // resolution failed so addr will be null and the DNS packet
        // response generator will substitute 0.0.0.0
        catch (Exception e)
        {
            logger.info("Unable to resolve " + packet.getQname());
        }

        // craft a DNS response pointing to the address we got back
        ByteBuffer bb = packet.GenerateResponse(addr);

        // send the packet to the client
        session.sendClientPacket(bb,event.header());

        // increment our counter and release the session
        node.incrementBlinger(CaptureNode.BlingerType.SESSPROXY,1);
        session.release();
    }
}
