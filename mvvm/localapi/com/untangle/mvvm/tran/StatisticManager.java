/*
 * Copyright (c) 2003-2006 Untangle, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Untangle, Inc. ("Confidential Information"). You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */
package com.untangle.mvvm.tran;

import com.untangle.mvvm.MvvmContextFactory;
import com.untangle.mvvm.MvvmLocalContext;
import com.untangle.mvvm.logging.EventLogger;
import com.untangle.mvvm.logging.StatisticEvent;
import org.apache.log4j.Logger;

public abstract class StatisticManager implements Runnable
{
    /* Log every five minutes */
    private static final long   SLEEP_TIME_MSEC = 5 * 60 * 1000;

    /* Delay a second while the thread is joining */
    private static final long   THREAD_JOIN_TIME_MSEC = 1000;

    /* The thread the monitor is running on */
    private Thread thread = null;

    /* Status of the monitor */
    private volatile boolean isAlive = true;

    protected final EventLogger eventLogger;
    private final Logger logger = Logger.getLogger( this.getClass());
    private final MvvmLocalContext localContext;

    protected StatisticManager(EventLogger eventLogger)
    {
        this.localContext = MvvmContextFactory.context();
        this.eventLogger = eventLogger;
    }

    protected StatisticManager(MvvmLocalContext localContext,
                               EventLogger eventLogger)
    {
        this.localContext = localContext;
        this.eventLogger = eventLogger;
    }

    public void run()
    {
        logger.debug( "Starting" );

        if ( !isAlive ) {
            logger.error( "died before starting" );
            return;
        }

        StatisticEvent statisticEvent = getInitialStatisticEvent();

        while ( true ) {
            try {
                Thread.sleep( SLEEP_TIME_MSEC );
            } catch ( InterruptedException e ) {
                logger.info( "Statistics manager was interrupted" );
            }

            /* Only log the stats if there is something to log */
            if ( statisticEvent.hasStatistics()) {
                /* Pre-cache the statistics to insure that no stats are lost */
                StatisticEvent currentStatistics = statisticEvent;
                statisticEvent = getNewStatisticEvent();
                eventLogger.log( currentStatistics );
            } else {
                logger.debug( "No statistics available" );
            }

            /* Check if the transform is still running */
            if ( !isAlive ) break;
        }

        logger.debug( "Finished" );
    }

    public synchronized void start()
    {
        isAlive = true;

        eventLogger.start();

        logger.debug( "Starting Statistic Manager" );

        /* If thread is not-null, there is a running thread that thinks it is alive */
        if ( thread != null ) {
            logger.debug( "Statistic Manager is already running" );
            return;
        }

        thread = this.localContext.newThread( this );
        thread.start();
    }

    public synchronized void stop()
    {
        if ( thread != null ) {
            logger.debug( "Stopping Statistic Manager" );

            isAlive = false;
            try {
                thread.interrupt();
                thread.join( THREAD_JOIN_TIME_MSEC );
            } catch ( SecurityException e ) {
                logger.error( "security exception, impossible", e );
            } catch ( InterruptedException e ) {
                logger.error( "interrupted while stopping", e );
            }
            thread = null;
        }

        eventLogger.stop();
    }

    protected abstract StatisticEvent getInitialStatisticEvent();

    protected abstract StatisticEvent getNewStatisticEvent();
}
