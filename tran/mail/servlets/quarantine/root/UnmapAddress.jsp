<%
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
%>
<%@ taglib uri="/WEB-INF/taglibs/quarantine_euv.tld" prefix="quarantine" %>



<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<!-- HEADING -->
  <title>Untangle | Aliases forwarding to <quarantine:currentAddress/></title>
    <script>
      function CheckAll() {
        count = document.form1.elements.length;
        isOn = document.form1.checkall.checked;
        for (i=0; i < count; i++) {
          document.form1.elements[i].checked = isOn;
        }
      }
    </script>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
  <link rel="stylesheet" href="styles/style.css" type="text/css"/>
</head>
<body>

<center>
<table border="0" cellpadding="0" cellspacing="0" width="904">

<!-- TOP THIRD -->
  <tbody>
    <tr>
      <td id="table_main_top_left"><img src="images/spacer.gif" alt=" " height="23" width="23"/><br/>
      </td>

      <td id="table_main_top" width="100%"><img src="images/spacer.gif" alt=" " height="1" width="1"/><br/>
      </td>

      <td id="table_main_top_right"> <img src="images/spacer.gif" alt=" " height="23" width="23"/><br/>
      </td>
    </tr>

    <!-- END TOP THIRD -->

    <!-- MIDDLE THIRD -->
    <tr>
      <td id="table_main_left"><img src="images/spacer.gif" alt=" " height="1" width="1"/></td>

      <td id="table_main_center">
        <table width="100%">
          <tbody>
            <tr>
              <td valign="middle" width="150">
                <a href="http://www.untangle.com">
                  <img src="images/Logo150x96.gif" border="0" alt="Untangle logo"/>
                </a>
              </td>

              <td style="padding: 0px 0px 0px 10px;" class="page_header_title" align="left" valign="middle">
        Aliases forwarding to:<br/><quarantine:currentAddress/>
              </td>
            </tr>
          </tbody>
        </table>
      </td>

      <td id="table_main_right"> <img src="images/spacer.gif" alt=" " height="1" width="1"/></td>
    </tr>

    <!-- END MIDDLE THIRD -->
    <!-- CONTENT AREA -->
    <tr>

      <td id="table_main_left"></td>

      <!-- CENTER CELL -->
      <td id="table_main_center" style="padding: 8px 0px 0px;">
        <hr size="1" width="100%"/>

        <!-- INTRO MESSAGE -->
    The following is a list of email addresses which have their quarantine emails forwarded to <quarantine:currentAddress/>.  To stop this from happening (to no longer have quarantine mails for one or more of these addresses forwarded), click the checkboxes for one or more addresses and click <code>Remove Selected Addresses</code>.

        <!-- INITIAL TABLE -->
        <center>
        <table>
              <quarantine:hasMessages type="info">
              <tr>
                <td>
                  <ul class="messageText">
                    <quarantine:forEachMessage type="info">
                      <li><quarantine:message/></li>
                    </quarantine:forEachMessage>
                  </ul>
                </td>
              </tr>
              </quarantine:hasMessages>
        </table>
        </center>
        <center>
        <table>
              <quarantine:hasMessages type="error">
              <tr>
                <td>
                  <ul class="errortext">
                    <quarantine:forEachMessage type="error">
                      <li><quarantine:message/></li>
                    </quarantine:forEachMessage>
                  </ul>
                </td>
              </tr>
              </quarantine:hasMessages>
        </table>
        </center>

        <!-- INPUT FORM 1 -->
          <form name="form1" method="POST" action="unmp">
            <input type="hidden"
              name="<quarantine:constants keyName="action"/>"
              value="unmapremove"/>
            <input type="hidden"
              name="<quarantine:constants keyName="tkn"/>"
              value="<quarantine:currentAuthToken encoded="false"/>"/>
            <table width="100%">
              <tr>
                <td>
                  <table class="slist">
                    <thead>
                      <tr>
                        <th><input name="checkall"
                          value="checkall"
                          onclick="CheckAll()"
                          type="checkbox"></th>
                        <th width="100%">Email Address</th>
                      </tr>
                    </thead>
                    <tbody>
                      <quarantine:forEachReceivingRemapsEntry>
                        <tr>
                          <td>
                            <input type="checkbox"
                              name="unmapaddr"
                              value="<quarantine:receivingRemapsEntry encoded="false"/>"/>
                          </td>
                          <td>
                            <quarantine:receivingRemapsEntry encoded="false"/>
                          </td>
                        </tr>
                      </quarantine:forEachReceivingRemapsEntry>
                    </tbody>
                    <tfoot>
                      <tr>
                        <td colspan="2"><input type="Submit" value="Remove Selected Addresses"/>
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </td>
              </tr>
            </table>
          </form>
          <br><br>



        <br/>
    <center>Powered by Untangle&reg; Platform</center>

          <hr size="1" width="100%"/>
        </td>
      <!-- END CENTER CELL -->
      <td id="table_main_right"></td>
    </tr>
    <!-- END CONTENT AREA -->

    <!-- BOTTOM THIRD -->
    <tr>
      <td id="table_main_bottom_left"><img src="images/spacer.gif" alt=" " height="23" width="23"/><br/>
      </td>
      <td id="table_main_bottom"><img src="images/spacer.gif" alt=" " height="1" width="1"/><br/>
      </td>
      <td id="table_main_bottom_right"> <img src="images/spacer.gif" alt=" " height="23" width="23"/><br/>
      </td>
    </tr>
    <!-- END BOTTOM THIRD -->
  </tbody>
</table>

</center>

<!-- END BRUSHED METAL PAGE BACKGROUND -->

</body>
</html>

