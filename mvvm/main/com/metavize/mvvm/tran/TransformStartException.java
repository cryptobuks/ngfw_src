/* 
 * Copyright (c) 2004 Metavize Inc.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 * 
 * $Id: TransformStartException.java,v 1.1.1.1 2004/12/01 23:32:22 amread Exp $
 */

package com.metavize.mvvm.tran;

public class TransformStartException extends TransformException 
{
    public TransformStartException() 
    { 
	super(); 
    }

    public TransformStartException(String message) 
    { 
	super(message); 
    }

    public TransformStartException(String message, Throwable cause) 
    { 
	super(message, cause); 
    }

    public TransformStartException(Throwable cause) 
    { 
	super(cause); 
    }
}
