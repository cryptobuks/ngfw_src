/*
 * Copyright (c) 2003 - 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id: IPSessionImpl.java,v 1.42 2005/03/22 01:05:54 jdi Exp $
 */

package com.metavize.mvvm.tapi.impl;

import com.metavize.mvvm.tapi.*;
import com.metavize.mvvm.tapi.event.IPStreamer;
import com.metavize.mvvm.tran.Transform;
import com.metavize.mvvm.tran.TransformState;
import com.metavize.mvvm.util.MetaEnv;
import com.metavize.jvector.IncomingSocketQueue;
import com.metavize.jvector.OutgoingSocketQueue;
import com.metavize.mvvm.argon.PipelineListener;
import com.metavize.mvvm.engine.Main;

import java.net.InetAddress;
import org.apache.log4j.*;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;

abstract class IPSessionImpl extends SessionImpl implements IPSession, PipelineListener {

    protected boolean released;

    protected Dispatcher dispatcher;

    protected List<ByteBuffer>[] bufs2write = new ArrayList[] { null, null };
    
    protected IPStreamer[] streamer = null;
    
    protected Logger logger;

    protected RWSessionStats stats;

    private Logger timesLogger = null;

    protected IPSessionImpl(Dispatcher disp, com.metavize.mvvm.argon.IPSession pSession)
    {
        super(disp.mPipe(), pSession);
        this.released = false;
        this.dispatcher = disp;
        this.stats = new RWSessionStats();
        if (RWSessionStats.DoDetailedTimes)
            timesLogger = Logger.getLogger("com.metavize.mvvm.tapi.SessionTimes");
        logger = disp.mPipe().sessionLogger();
    }
    
    public InetAddress clientAddr()
    {
        return ((com.metavize.mvvm.argon.IPSession)pSession).clientAddr();
    }

    public InetAddress serverAddr()
    {
        return ((com.metavize.mvvm.argon.IPSession)pSession).serverAddr();
    }

    public int clientPort()
    {
        return ((com.metavize.mvvm.argon.IPSession)pSession).clientPort();
    }

    public int serverPort()
    {
        return ((com.metavize.mvvm.argon.IPSession)pSession).serverPort();
    } 

    public SessionStats stats()
    {
        return stats;
    }

    // This one is for releasing once the session has been alive.
    public void release()
    {
        cancelTimer();

        /** Someday...
        try {
            Mnp req = RequestUtil.createReleaseNewSession();
            ReleaseNewSessionType rl = req.getReleaseNewSession();
            rl.setSessId(id);
            xenon.requestNoReply(req);
        } catch (XenonException x) {
            // Not expected, just log
            xenon.sessionLogger().warn("Exception releasing new session", x);
        }
        */
        released = true;
        // Do more eventually (closing sockets.) XX
    }

    public byte clientIntf()
    {
        return ((com.metavize.mvvm.argon.IPSession)pSession).clientIntf();
    }

    public byte serverIntf()
    {
        return ((com.metavize.mvvm.argon.IPSession)pSession).serverIntf();
    }

    public boolean released()
    {
        return released;
    }

    public void scheduleTimer(long delay)
    {
        if (delay < 0)
            throw new IllegalArgumentException("Delay must be non-negative");
        // xenon.scheduleTimer(this, delay);
    }

    public void cancelTimer()
    {
        // xenon.cancelTimer(this);
    }

    // XX Only works for max two interfaces.
    public byte direction()
    {
        if (clientIntf() == 0)
            return INBOUND;
        else
            return OUTBOUND;
    }

    protected ByteBuffer getNextBuf2Send(int side) {
        List<ByteBuffer> bufs = bufs2write[side];
        assert bufs != null;
        ByteBuffer result = bufs.get(0);
        assert result != null;
        assert result.remaining() > 0 : "Cannot send zero length buffer";
        int len = bufs.size() - 1;
        if (len == 0) {
            // Check if we sent em all, and if so remove the array.
            bufs2write[side] = null;
        } else {
            bufs.remove(0);
        }
        return result;
    }

    protected void addBufs(int side, ByteBuffer[] new2send)
    {
        if (new2send == null || new2send.length == 0)
            return;
        for (int i = 0; i < new2send.length; i++)
            addBuf(side, new2send[i]);
    }

    protected void addBuf(int side, ByteBuffer buf)
    {
        if (buf == null || buf.remaining() == 0)
            // Skip it.
            return;

        List<ByteBuffer> bufs = bufs2write[side];

        if (bufs == null) {
            bufs = new ArrayList<ByteBuffer>();
            bufs2write[side] = bufs;
        }
        bufs.add(buf);
    }

    public void raze()
    {
        Transform xform = mPipe().transform();
        if (xform.getRunState() != TransformState.RUNNING) {
            String message = "killing: raze for transform in state " + xform.getRunState();
            warn(message);
            // No need to kill the session, it's already dead.
            // killSession(message);
            return;
        }
        ClassLoader classLoader = xform.getClass().getClassLoader();
        Thread ct = Thread.currentThread();
        ClassLoader oldCl = ct.getContextClassLoader();

        // entering TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ct.setContextClassLoader(classLoader);
        try {
            if (logger.isDebugEnabled()) {
                IncomingSocketQueue ourcin = ((com.metavize.mvvm.argon.Session)pSession).clientIncomingSocketQueue();
                IncomingSocketQueue oursin = ((com.metavize.mvvm.argon.Session)pSession).serverIncomingSocketQueue();
                OutgoingSocketQueue ourcout = ((com.metavize.mvvm.argon.Session)pSession).clientOutgoingSocketQueue();
                OutgoingSocketQueue oursout = ((com.metavize.mvvm.argon.Session)pSession).serverOutgoingSocketQueue();
                debug("raze ourcin: " + ourcin +
                      ", ourcout: " + ourcout + ", ourcsin: " + oursin + ", oursout: " + oursout +
                      "  /  bufs[CLIENT]: " + bufs2write[CLIENT] + ", bufs[SERVER]: " + bufs2write[SERVER]);
            }
            closeFinal();
        } finally {
            ct.setContextClassLoader(oldCl);
            // left TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
    }

    public void clientEvent(IncomingSocketQueue in)
    {
        Transform xform = mPipe().transform();
        if (xform.getRunState() != TransformState.RUNNING) {
            String message = "killing: clientEvent(in) for transform in state " + xform.getRunState();
            warn(message);
            killSession(message);
            return;
        }
        ClassLoader classLoader = xform.getClass().getClassLoader();
        Thread ct = Thread.currentThread();
        ClassLoader oldCl = ct.getContextClassLoader();

        // entering TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ct.setContextClassLoader(classLoader);
        try {
            readEvent(CLIENT, in);
        } finally {
            ct.setContextClassLoader(oldCl);
            // left TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
    }

    public void serverEvent(IncomingSocketQueue in)
    {
        Transform xform = mPipe().transform();
        if (xform.getRunState() != TransformState.RUNNING) {
            String message = "killing: serverEvent(in) for transform in state " + xform.getRunState();
            warn(message);
            killSession(message);
            return;
        }
        ClassLoader classLoader = xform.getClass().getClassLoader();
        Thread ct = Thread.currentThread();
        ClassLoader oldCl = ct.getContextClassLoader();

        // entering TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ct.setContextClassLoader(classLoader);
        try {
            readEvent(SERVER, in);
        } finally {
            ct.setContextClassLoader(oldCl);
            // left TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
    }

    public void clientEvent(OutgoingSocketQueue out)
    {
        Transform xform = mPipe().transform();
        if (xform.getRunState() != TransformState.RUNNING) {
            String message = "killing: clientEvent(out) for transform in state " + xform.getRunState();
            warn(message);
            killSession(message);
            return;
        }
        ClassLoader classLoader = xform.getClass().getClassLoader();
        Thread ct = Thread.currentThread();
        ClassLoader oldCl = ct.getContextClassLoader();

        // entering TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ct.setContextClassLoader(classLoader);
        try {
            writeEvent(CLIENT, out);
        } finally {
            ct.setContextClassLoader(oldCl);
            // left TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
    }

    public void serverEvent(OutgoingSocketQueue out)
    {
        Transform xform = mPipe().transform();
        if (xform.getRunState() != TransformState.RUNNING) {
            String message = "killing: serverEvent(out) for transform in state " + xform.getRunState();
            warn(message);
            killSession(message);
            return;
        }
        ClassLoader classLoader = xform.getClass().getClassLoader();
        Thread ct = Thread.currentThread();
        ClassLoader oldCl = ct.getContextClassLoader();

        // entering TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ct.setContextClassLoader(classLoader);
        try {
            writeEvent(SERVER, out);
        } finally {
            ct.setContextClassLoader(oldCl);
            // left TransformClassLoader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
    }

    /**
     * This one sets up the socket queues for streaming to begin.
     *
     */
    private void setupForStreaming()
    {
        IncomingSocketQueue cin = ((com.metavize.mvvm.argon.Session)pSession).clientIncomingSocketQueue();
        IncomingSocketQueue sin = ((com.metavize.mvvm.argon.Session)pSession).serverIncomingSocketQueue();
        OutgoingSocketQueue cout = ((com.metavize.mvvm.argon.Session)pSession).clientOutgoingSocketQueue();
        OutgoingSocketQueue sout = ((com.metavize.mvvm.argon.Session)pSession).serverOutgoingSocketQueue();
        assert (streamer != null);
        
        if (cin != null)
            cin.disable();
        if (sin != null)
            sin.disable();
        if (streamer[CLIENT] != null) {
            if (cout != null)
                cout.enable();
            if (sout != null)
                sout.disable();
        } else {
            if (sout != null)
                sout.enable();
            if (cout != null)
                cout.disable();
        }
    }

    /**
     * This one sets up the socket queues for normal operation; used when streaming ends.
     *
     */
    private void setupForNormal()
    {
        IncomingSocketQueue cin = ((com.metavize.mvvm.argon.Session)pSession).clientIncomingSocketQueue();
        IncomingSocketQueue sin = ((com.metavize.mvvm.argon.Session)pSession).serverIncomingSocketQueue();
        OutgoingSocketQueue cout = ((com.metavize.mvvm.argon.Session)pSession).clientOutgoingSocketQueue();
        OutgoingSocketQueue sout = ((com.metavize.mvvm.argon.Session)pSession).serverOutgoingSocketQueue();
        assert (streamer == null);

        // We take care not to change the state unless it's really changing, as changing the
        // state calls notifymvpoll() every time.
        if (sout != null && !sout.isEnabled())
            sout.enable();
        if (sout == null || (sout.isEmpty() && bufs2write[SERVER] == null)) {
            if (cin != null && !cin.isEnabled())
                cin.enable();
        } else {
            if (cin != null && cin.isEnabled())
                cin.disable();
        }
        if (cout != null && !cout.isEnabled())
            cout.enable();
        if (cout == null || (cout.isEmpty() && bufs2write[CLIENT] == null)) {
            if (sin != null && !sin.isEnabled())
                sin.enable();
        } else {
            if (sin != null && sin.isEnabled())
                sin.disable();
        }
    }

    /**
     * Returns true if we did something.
     *
     * @param side an <code>int</code> value
     * @param out an <code>OutgoingSocketQueue</code> value
     * @return a <code>boolean</code> value
     */
    private boolean doWrite(int side, OutgoingSocketQueue out)
        throws MPipeException
    {
        boolean didSomething = false;
        if (out != null && out.isEmpty()) {
            if (bufs2write[side] != null) {
                // Do this first, before checking streamer, so we drain out any remaining buffer.
                tryWrite(side, out, true);
                didSomething = true;
            } else if (streamer != null) {
                IPStreamer s = streamer[side];
                if (s != null) {
                    // It's the right one.
                    addStreamBuf(side, s);
                    if (bufs2write[side] != null) {
                        tryWrite(side, out, true);
                        didSomething = true;
                    }
                }
            }
        }
        return didSomething;
    }
                
    // This is the main write hook called by the Vectoring machine
    public void writeEvent(int side, OutgoingSocketQueue out)
    {
        String sideName = side == CLIENT ? "client" : "server";
        try {
            assert out != null;
            if (!out.isEmpty()) {
                warn("writeEvent to full outgoing queue on: " + sideName);
                return;
            }

            IncomingSocketQueue ourin;
            OutgoingSocketQueue ourout, otherout;
            if (side == CLIENT) {
                ourin = ((com.metavize.mvvm.argon.Session)pSession).serverIncomingSocketQueue();
                ourout = ((com.metavize.mvvm.argon.Session)pSession).clientOutgoingSocketQueue();
                otherout = ((com.metavize.mvvm.argon.Session)pSession).serverOutgoingSocketQueue();
            } else {
                ourin = ((com.metavize.mvvm.argon.Session)pSession).clientIncomingSocketQueue();
                ourout = ((com.metavize.mvvm.argon.Session)pSession).serverOutgoingSocketQueue();
                otherout = ((com.metavize.mvvm.argon.Session)pSession).clientOutgoingSocketQueue();
            }
            assert out == ourout;

            if (logger.isDebugEnabled()) {
                debug("write(" + sideName + ") out: " + out +
                      "   /  bufs, write-queue  " +  bufs2write[side] + ", " + out.numEvents() +
                      "(" + out.numBytes() + " bytes)" + "   opp-read-queue: " +
                      (ourin == null ? null : ourin.numEvents()));
            }

            if (!doWrite(side, ourout)) {
                sendWritableEvent(side);
                // We have to try more writing here in case we added stuff.
                doWrite(side, ourout);
                doWrite(1 - side, otherout);
            }
            if (streamer == null)
                setupForNormal();
        } catch (MPipeException x) {
            String message = "MPipeException while writing to " + sideName;
            warn(message, x);
            killSession(message);
        } catch (Exception x) {
            String message = "" + x.getClass().getName() + " while writing to " + sideName;
            warn(message, x);
            killSession(message);
        } catch (OutOfMemoryError x) {
            Main.fatalError("SessionHandler", x);
        }
    }


    public void readEvent(int side, IncomingSocketQueue in)
    {
        String sideName = side == CLIENT ? "client" : "server";
        try {
            assert in != null;
            IncomingSocketQueue ourin, otherin;
            OutgoingSocketQueue ourout, otherout;
            OutgoingSocketQueue cout = ((com.metavize.mvvm.argon.Session)pSession).clientOutgoingSocketQueue();
            OutgoingSocketQueue sout = ((com.metavize.mvvm.argon.Session)pSession).serverOutgoingSocketQueue();
            if (side == CLIENT) {
                ourin = ((com.metavize.mvvm.argon.Session)pSession).clientIncomingSocketQueue();
                otherin = ((com.metavize.mvvm.argon.Session)pSession).serverIncomingSocketQueue();
                ourout = sout;
                otherout = cout;
            } else {
                ourin = ((com.metavize.mvvm.argon.Session)pSession).serverIncomingSocketQueue();
                otherin = ((com.metavize.mvvm.argon.Session)pSession).clientIncomingSocketQueue();
                ourout = cout;
                otherout = sout;
            }
            assert in == ourin;

            if (logger.isDebugEnabled()) {
                debug("read(" + sideName + ") in: " + in +
                      "   /  opp-write-bufs: " + bufs2write[1 - side] + ", opp-write-queue: " +
                      (ourout == null ? null : ourout.numEvents()));
            }

            // need to check if input contains RST (for TCP) or EXPIRE (for UDP)
            // independent of the write buffers.
            if (sideDieing(side, in))
                return;

            assert streamer == null : "readEvent when streaming";;

            if (ourout == null || (bufs2write[1 - side] == null && ourout.isEmpty())) {
                tryRead(side, in, true);
                doWrite(side, otherout);
                doWrite(1 - side, ourout);
                if (streamer != null) {
                    // We do this after the writes so that we try to write out first.
                    setupForStreaming();
                    return;
                }
            } else {
                error("Illegal State: read(" + sideName + ") in: " + in +
                      "   /  opp-write-bufs: " + bufs2write[1 - side] + ", opp-write-queue: " +
                      (ourout == null ? null : ourout.numEvents()));
            }
            setupForNormal();
        } catch (MPipeException x) {
            String message = "MPipeException while reading from " + sideName;
            warn(message, x);
            killSession(message);
        } catch (Exception x) {
            String message = "" + x.getClass().getName() + " while reading from " + sideName;
            warn(message, x);
            killSession(message);
        } catch (OutOfMemoryError x) {
            Main.fatalError("SessionHandler", x);
        }
    }

    // Callback called on finalize
    protected void closeFinal()
    {   
        cancelTimer();
        if (RWSessionStats.DoDetailedTimes) {
            long[] times = stats().times();
            times[SessionStats.FINAL_CLOSE] = MetaEnv.currentTimeMillis();
            if (timesLogger.isInfoEnabled())
                reportTimes(times);
        }

        dispatcher.removeSession(this);
    }

    protected void error(String message)
    {
        StringBuffer msg = logPrefix();
        msg.append(message);
        logger.error(msg.toString());
    }

    protected void warn(String message)
    {
        StringBuffer msg = logPrefix();
        msg.append(message);
        logger.warn(msg.toString());
    }

    protected void warn(String message, Exception x)
    {
        StringBuffer msg = logPrefix();
        msg.append(message);
        logger.warn(msg.toString(), x);
    }

    protected void info(String message)
    {
        if (logger.isInfoEnabled()) {
            StringBuffer msg = logPrefix();
            msg.append(message);
            logger.info(msg.toString());
        }
    }

    protected void debug(String message)
    {
        if (logger.isDebugEnabled()) {
            StringBuffer msg = logPrefix();
            msg.append(message);
            logger.debug(msg.toString());
        }
    }

    protected void debug(String message, Exception x)
    {
        if (logger.isDebugEnabled()) {
            StringBuffer msg = logPrefix();
            msg.append(message);
            logger.debug(msg.toString(), x);
        }
    }

    /**
     * <code>containsEnding</code> returns true if the incoming socket queue contains an event
     * that will cause the end of the session (at least on that side).  These are RST for TCP
     * and EXPIRE for UDP.  It also sends the event to the user.
     *
     * @param in an <code>IncomingSocketQueue</code> value
     * @return a <code>boolean</code> value
     */
    abstract boolean sideDieing(int side, IncomingSocketQueue in) throws MPipeException;

    abstract void killSession(String message);

    public abstract IPSessionDesc makeDesc();

    abstract void sendWritableEvent(int side) throws MPipeException;

    abstract void tryWrite(int side, OutgoingSocketQueue out, boolean warnIfUnable)
        throws MPipeException;

    abstract void addStreamBuf(int side, IPStreamer streamer)
        throws MPipeException;

    abstract void tryRead(int side, IncomingSocketQueue in, boolean warnIfUnable)
        throws MPipeException;

    abstract StringBuffer logPrefix();

    private static DateFormat formatter = new AbsoluteTimeDateFormat();

    private void reportTimes(long[] times) {
        StringBuffer result = new StringBuffer("times for ");
        result.append(id());
        result.append("\n");
        
        for (int i = SessionStats.MIN_TIME_INDEX; i < SessionStats.MAX_TIME_INDEX; i++) {
            if (times[i] == 0)
                continue;
            String name = SessionStats.TimeNames[i];
            int len = name.length();
            int pad = 30 - len;
            result.append(name);
            result.append(": ");
            for (int j = 0; j < pad; j++)
                result.append(' ');
            formatter.format(new Date(times[i]), result, null);
            result.append("\n");
        }
        timesLogger.info(result.toString());
    }


    // Don't need equal or hashcode since we can only have one of these objects per
    // session (so the memory address is ok for equals/hashcode).
}
