/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: Sink.c,v 1.1 2005/01/30 00:07:38 rbscott Exp $
 */

#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <libmvutil.h>
#include <libvector.h>
#include <mvutil/debug.h>
#include <mvutil/mvpoll.h>
#include <mvutil/errlog.h>
#include <mvutil/debug.h>
#include <vector/event.h>
#include <vector/source.h>

#include "jni_header.h"
#include "jvector.h"
#include "jerror.h"

#include JH_Sink

JNIEXPORT void JNICALL JF_Sink( raze )
    ( JNIEnv *env, jobject _this, jint pointer )
{
    jvector_sink_t* jv_snk = (jvector_sink_t*)pointer;
    
    if ( jv_snk == NULL ) {
        errlogargs();
        return;
    }

    if ( jv_snk->key != NULL ) 
        mvpoll_key_raze( jv_snk->key );

    jv_snk->key = NULL;

    if ( jv_snk->this != NULL )
        (*env)->DeleteGlobalRef( env, jv_snk->this );

    jv_snk->this = NULL;

    free( jv_snk );
}

