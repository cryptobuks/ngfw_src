/*
 * Copyright (c) 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id$
 */
package com.metavize.tran.mail.papi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.metavize.tran.mime.*;
import org.apache.log4j.Logger;

/**
 * Log e-mail message info.
 *
 * @author <a href="mailto:cng@metavize.com">C Ng</a>
 * @version 1.0
 * @hibernate.class
 * table="TR_MAIL_MESSAGE_INFO"
 * mutable="false"
 */
public class MessageInfo implements Serializable
{
    /* constants */
    public static final int SMTP_PORT = 25;
    public static final int POP3_PORT = 110;
    public static final int IMAP4_PORT = 143;

    // How big a varchar() do we get for default String fields.  This
    // should be elsewhere. XXX
    public static final int DEFAULT_STRING_SIZE = 255;

    // private static final Logger zLog = Logger.getLogger(MessageInfo.class.getName());

    /* columns */
    private Long id; /* msg_id */

    private int sessionId; /* s_id */
    // private MLHandlerInfo handlerInfo; /* hdl_id */

    private String subject;

    private char serverType;

    /* Senders/Receivers */
    private Set addresses = new HashSet();

    /* non-persistent fields */
    public Map counts = new HashMap();

    /* constructors */
    public MessageInfo() {}

    public MessageInfo(int sessionId, int serverPort, String subject)
    {
        // Subject really shouldn't be NOT NULL, but it's easier for
        // now to fix by using an empty string... XXX jdi 8/9/05
        if (subject == null)
            subject = "";

        if (subject != null && subject.length() > DEFAULT_STRING_SIZE) {
            subject = subject.substring(0, DEFAULT_STRING_SIZE);
        }
        this.subject = subject;
        this.sessionId = sessionId;

        switch (serverPort) {
        case SMTP_PORT:
            serverType = 'S';
            break;
        case POP3_PORT:
            serverType = 'P';
            break;
        case IMAP4_PORT:
            serverType = 'I';
            break;
        default:
            serverType = 'U';
            break;
        }
    }

    /* Business methods */
    public void addAddress(AddressKind kind, String address, String personal)
    {
        Integer p = (Integer)counts.get(kind);
        if (null == p) {
            p = 0;
        }
        counts.put(kind, ++p);

        MessageInfoAddr newAddr = new MessageInfoAddr(this, p, kind, address, personal);
        addresses.add(newAddr);
    }

    /* public methods */

    /**
     *
     * @hibernate.id
     * column="ID"
     * generator-class="native"
     */
    private Long getId()
    {
        return id;
    }

    private void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Set of the addresses involved (to, from, etc) in the email.
     *
     * @return the set of the email addresses involved in the email
     * @hibernate.set
     * inverse="true"
     * cascade="all-delete-orphan"
     * @hibernate.collection-key
     * column="MSG_ID"
     * @hibernate.collection-one-to-many
     * class="com.metavize.tran.mail.papi.MessageInfoAddr"
     */
    public Set getAddresses()
    {
        return addresses;
    }

    public void setAddresses(Set s)
    {
        addresses = s;
    }

    /**
     * Session id.
     *
     * @return the session id.
     * @hibernate.property
     * column="SESSION_ID"
     */
    public int getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(int sessionId)
    {
        this.sessionId = sessionId;
    }

    /**
     * Identify RFC822 Subject.
     *
     * @return RFC822 Subject.
     * @hibernate.property
     * column="SUBJECT"
     * not-null="true"
     */
    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        if (subject != null && subject.length() > DEFAULT_STRING_SIZE) {
            subject = subject.substring(0, DEFAULT_STRING_SIZE);
        }
        this.subject = subject;
    }

    /**
     * Identify server type (SMTP, POP3, or IMAP4).
     *
     * @return server type.
     * @hibernate.property
     * column="SERVER_TYPE"
     * type="char"
     * length="1"
     * not-null="true"
     */
    public char getServerType()
    {
        return serverType;
    }

    public void setServerType(char serverType)
    {
        this.serverType = serverType;
    }
}
