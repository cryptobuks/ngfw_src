/*
 * Copyright (c) 2003,2004 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: mvsem.h,v 1.3 2004/11/16 01:11:21 dmorris Exp $
 */
#ifndef __MVSEM_H
#define __MVSEM_H

#include <semaphore.h>
#include "mvpoll.h"

#define MVSEM_MVPOLL_KEY_TYPE 100

typedef struct mvsem {

    mvpoll_key_t* key;

    sem_t sem;

} mvsem_t;


int      mvsem_init   (mvsem_t* sem, int share, int value);
mvsem_t* mvsem_create (int share, int value);
mvsem_t* mvsem_malloc (void);

int      mvsem_destroy (mvsem_t* sem);
int      mvsem_raze (mvsem_t* sem);
void     mvsem_free (mvsem_t* sem);

int      mvsem_post (mvsem_t* mvsem);
int      mvsem_wait (mvsem_t* mvsem);
int      mvsem_trywait (mvsem_t* mvsem);

#endif
