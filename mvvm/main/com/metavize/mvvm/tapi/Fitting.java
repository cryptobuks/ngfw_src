/*
 * Copyright (c) 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: Fitting.java,v 1.1 2005/01/30 09:20:31 amread Exp $
 */

package com.metavize.mvvm.tapi;

/**
 * Describes the type of the stream between two transforms.
 *
 * @author <a href="mailto:amread@metavize.com">Aaron Read</a>
 * @version 1.0
 */
public class Fitting
{
    public static final Fitting OCTET_STREAM = new Fitting("octet-stream");
    public static final Fitting HTTP_STREAM
        = new Fitting("http-stream", OCTET_STREAM);
    public static final Fitting FTP_STREAM
        = new Fitting("ftp-stream", OCTET_STREAM);

    public static final Fitting TOKEN_STREAM = new Fitting("token-stream");
    public static final Fitting HTTP_TOKENS
        = new Fitting("http-tokens", TOKEN_STREAM);

    private String type;
    private Fitting parent;

    // constructors -----------------------------------------------------------

    private Fitting(String type, Fitting parent)
    {
        this.type = type;
        this.parent = parent;
    }

    private Fitting(String type)
    {
        this.type = type;
        this.parent = null;
    }

    // factories --------------------------------------------------------------

    // XXX define some factories for defining & retrieving fittings

    // public methods --------------------------------------------------------

    public Fitting getParent()
    {
        return parent;
    }

    public boolean instanceOf(Fitting o)
    {
        Fitting t = this;
        while (null != t) {
            if (t == o) {
                return true;
            }

            t = t.getParent();
        }

        return false;
    }

    // Object methods ---------------------------------------------------------

    public String toString()
    {
        return "Fitting: " + type + " parent: " + parent;
    }
}
