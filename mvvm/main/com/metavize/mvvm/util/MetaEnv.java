/*
 * Copyright (c) 2003 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id: MetaEnv.java,v 1.1 2005/01/01 00:39:29 jdi Exp $
 */

package com.metavize.mvvm.util;

import java.util.Random;

public class MetaEnv {

    private static Random rng;

    public static long currentTimeMillis()
    {   
        // if in testing mode, return fake time, else:
        return System.currentTimeMillis();
    }

    public static void initRNG(long seed)
    {
        rng = new Random(seed);
    }

    public static void initRNG()
    {
        rng = new Random();
    }

    public static Random rng()
    {
        // Could lock here, but it's one-time kinda stuff so not very unsafe. XX
        if (rng == null) {
            initRNG();
        }
        return rng;
    }
}
