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

package com.untangle.mvvm.engine;

import com.untangle.mvvm.MvvmContextFactory;
import com.untangle.mvvm.logging.LogEvent;
import com.untangle.mvvm.logging.LoggingManager;
import com.untangle.mvvm.logging.LoggingSettings;
import com.untangle.mvvm.util.TransactionWork;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

class LoggingManagerImpl implements LoggingManager
{
    private static final Object LOCK = new Object();
    private static final LogEvent[] LOG_PROTO = new LogEvent[0];

    private static LoggingManagerImpl LOGGING_MANAGER;

    private final Logger logger = Logger.getLogger(getClass());

    private LoggingSettings loggingSettings;

    private LoggingManagerImpl()
    {
        TransactionWork tw = new TransactionWork()
            {
                public boolean doWork(Session s)
                {
                    Query q = s.createQuery("from LoggingSettings ls");
                    loggingSettings = (LoggingSettings)q.uniqueResult();

                    if (null == loggingSettings) {
                        loggingSettings = new LoggingSettings();
                        s.save(loggingSettings);
                    }

                    return true;
                }
            };
        MvvmContextFactory.context().runTransaction(tw);

        SyslogManagerImpl.manager().reconfigure(loggingSettings);
    }

    static LoggingManagerImpl loggingManager()
    {
        synchronized (LOCK) {
            if (null == LOGGING_MANAGER) {
                LOGGING_MANAGER = new LoggingManagerImpl();
            }
        }

        return LOGGING_MANAGER;
    }

    public LoggingSettings getLoggingSettings()
    {
        return loggingSettings;
    }

    public void setLoggingSettings(final LoggingSettings loggingSettings)
    {
        this.loggingSettings = loggingSettings;

        TransactionWork tw = new TransactionWork()
            {
                public boolean doWork(Session s)
                {
                    s.merge(loggingSettings);
                    return true;
                }
            };

        MvvmContextFactory.context().runTransaction(tw);

        SyslogManagerImpl.manager().reconfigure(loggingSettings);
    }

    public void resetAllLogs()
    {
        MvvmRepositorySelector.get().reconfigureAll();
    }

    public void logError(String errorText)
    {
        if (null == errorText) {
           logger.error("This is the default error text.");
        } else {
           logger.error(errorText);
        }

        return;
    }
}
