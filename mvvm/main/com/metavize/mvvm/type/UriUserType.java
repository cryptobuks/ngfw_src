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

package com.metavize.mvvm.type;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;

public class UriUserType implements UserType
{
    private static final int[] SQL_TYPES = { Types.VARCHAR };

    public int[] sqlTypes() { return SQL_TYPES; }
    public Class returnedClass() { return URI.class; }
    public boolean equals(Object x, Object y) { return x.equals(y); }
    public Object deepCopy(Object value) { return value; }
    public boolean isMutable() { return false; }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
        throws HibernateException, SQLException
    {
        String name = rs.getString(names[0]);

        if (rs.wasNull()) {
            return null;
        } else {
            // could have been truncated in middle of escape
            if (1 <= name.length() && '%' == name.charAt(name.length() - 1)) {
                name = name.substring(0, name.length() - 1);
            } else if (2 <= name.length() && '%' == name.charAt(name.length() - 2)) {
                name = name.substring(0, name.length() - 2);
            }
            try {
                return new URI(name);
            } catch (URISyntaxException exn) {
                throw new HibernateException(exn);
            }
        }
    }

    public void nullSafeSet(PreparedStatement ps, Object v, int i)
        throws HibernateException, SQLException
    {
        if (null == v) {
            ps.setNull(i, Types.VARCHAR);
        } else {
            // XXX we don't know the column length,
            // XXX should we break uri's into multiple columns? just path?
            String s = v.toString();

            ps.setString(i, s.length() < 255 ? s : s.substring(0, 255));
        }
    }
}
