/*
 * Copyright (c) 2003 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: event.c,v 1.2 2005/01/27 04:55:32 rbscott Exp $
 */
#include "event.h"

#include <stdlib.h>
#include <mvutil/errlog.h>

event_t* event_create (event_type_t type)
{
    event_t* ev = malloc(sizeof(event_t));
    if (!ev)
        return errlogmalloc_null();

    ev->type = type;
    ev->raze = event_raze;
    
    return ev;
}

void     event_raze (event_t* ev)
{
    if (ev) free(ev);
}
