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
package com.untangle.tran.mail.web.euv;

import com.untangle.tran.mail.papi.quarantine.InboxRecordComparator;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletRequest;

/**
 * Utility methods
 */
public class Util {

  private static HashMap<String, InboxRecordComparator.SortBy> m_stringToSB
    = new HashMap<String, InboxRecordComparator.SortBy>();
  private static HashMap<InboxRecordComparator.SortBy, String> m_sbToString
    = new HashMap<InboxRecordComparator.SortBy, String>();

  static {
    InboxRecordComparator.SortBy[] allSortings =
      InboxRecordComparator.SortBy.values();
    for(int i = 0; i<allSortings.length; i++) {
      String key = Integer.toString(i);
      m_stringToSB.put(key, allSortings[i]);
      m_sbToString.put(allSortings[i], key);
    }
  }

  public static String sortByToString(InboxRecordComparator.SortBy sb) {
    return m_sbToString.get(sb);
  }

  /**
   * I didn't want the actual Strings from the
   * enum to travel to the client, but for now I'll
   * use this little hack.  This goes from String to
   * SortBy.  {@link #sortByToString sortByToString} does
   * the reverse.
   * <br><br>
   * @param s the String representation of sorting
   * @param def the default, should <code>s</code> not be 
   *        convertable to a SortBy
   */
  public static InboxRecordComparator.SortBy stringToSortBy(
    String s,
    InboxRecordComparator.SortBy def) {

    if(s == null) {
      return def;
    }
    s = s.trim().toLowerCase();
    
    InboxRecordComparator.SortBy ret = m_stringToSB.get(s);
    
    return ret==null?
      def:ret;
  }

  /**
   * Read a boolean parameter
   */
  public static boolean readBooleanParam(ServletRequest req,
    String paramName,
    boolean def) {
    if(req.getParameter(paramName) == null) {
      return def;
    }
    try {
      return Boolean.parseBoolean(req.getParameter(paramName));
    }
    catch(Exception ex) {
    }
    return def;
  }  

  /**
   * Read an int parameter
   */
  public static int readIntParam(ServletRequest req,
    String paramName,
    int def) {
    try {
      return Integer.parseInt(req.getParameter(paramName));
    }
    catch(Exception ex) {
    }
    return def;
  }
}