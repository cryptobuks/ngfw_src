/*
 * Copyright (c) 2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: MackageInstallException.java,v 1.1.1.1 2004/12/01 23:32:21 amread Exp $
 */

package com.metavize.mvvm;

public class MackageInstallException extends MackageException
{
    public MackageInstallException()
    {
        super();
    }

    public MackageInstallException(String message)
    {
        super(message);
    }

    public MackageInstallException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MackageInstallException(Throwable cause)
    {
        super(cause);
    }
}
