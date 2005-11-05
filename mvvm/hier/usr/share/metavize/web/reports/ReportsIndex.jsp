
<%@ page language="java" import="com.metavize.mvvm.*, com.metavize.mvvm.client.*, com.metavize.mvvm.security.Tid, com.metavize.mvvm.tran.*, com.metavize.mvvm.tapi.*, com.metavize.mvvm.util.SessionUtil, org.apache.log4j.helpers.AbsoluteTimeDateFormat, java.util.Properties, java.net.URL, java.io.*, java.util.Vector, java.util.Collections, java.util.Comparator, javax.naming.*" %>

<%

	// ENUMERATE THE DIRECTORIES IN REPORTS
	String path = System.getProperty("bunnicula.home") + "/web/reports";
	File mvvmReportsFile = new File(path);
	Vector<File> directories = new Vector<File>();
	File[] allFiles = mvvmReportsFile.listFiles();
	for( File file : allFiles ){
		if( file.isDirectory()
		    && (file.getName().charAt(4) == '-')
		    && (file.getName().charAt(7) == '-') )
			directories.add(file);
	}

	// ORDER THE DIRECTORIES
	Comparator<File> directoryComparator = new Comparator<File>(){
		public int compare(File f1, File f2){ 
			String n1 = f1.getName();
			String n2 = f2.getName();
			try{
				int y1 = Integer.parseInt(n1.substring(0, 4));
				int y2 = Integer.parseInt(n2.substring(0, 4));
				if( y1 > y2 )
					return -1;
				else if( y1 < y2 )
					return 1;
				int m1 = Integer.parseInt(n1.substring(5, 7));
				int m2 = Integer.parseInt(n2.substring(5, 7));
				if( m1 > m2 )
					return -1;
				else if( m1 < m2 )
					return 1;
				int d1 = Integer.parseInt(n1.substring(8, 10));
				int d2 = Integer.parseInt(n2.substring(8, 10));
				if( d1 > d2 )
					return -1;
				else if( d1 < d2 )
					return 1;
				else
					return 0;
			}
			catch(Exception e){ return 0; }
		}
		public boolean equals(Object o){ return true; }
		};
	Collections.sort(directories, directoryComparator);
	

%>

<html xmlns="http://www.w3.org/1999/xhtml">

  <!-- HEADING -->
  <head>
    <title>Metavize EdgeReport</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <style type="text/css">
    <!--
body {
     margin: 15px;
     background: #FFFFFF url(./images/background_body.gif) repeat-x top left;
     text-align: center;
}

table {
    font: normal normal normal 10pt/14pt Arial,Sans-Serif;
    text-align: left;
    color: #606060;
}

input {
    font: normal normal normal 10pt/14pt Arial,Sans-Serif;
    text-align: left;
    color: #606060;
}

form {
    margin: 0px;
}

a:link, a:visited {
    font: normal normal normal 10pt/14pt Arial,Sans-Serif;
    color: #1997FE;
    text-decoration: none;
}

a:hover {
    color: #1997FE;
    text-decoration: underline;
}

.page_header_title {
    font: bold normal normal 25pt Arial,Sans-Serif;
    color: #777777;
}

.page_header_alternate {
    font: normal normal normal 20pt Arial,Sans-Serif;
    color: #777777;
}


h1 {
    font: normal normal bold 11pt Arial,Sans-Serif;
    color: #999999;
    letter-spacing: 1px;
    margin: 0px;
    padding: 0px;
}

h2 {
    font: normal normal bold 10pt Arial,Sans-Serif;
    color: #999999;
    letter-spacing: 1px;
    margin: 0px;
    padding: 0px;
}

h3 {
    font: normal normal bold 11pt Arial,Sans-Serif;
    color: #606060;
    letter-spacing: 1px;
    margin: 0px;
    padding: 0px;
}

h4 {
    font: normal normal bold 14pt Arial,Sans-Serif;
    color: #0044D7;
    letter-spacing: 1px;
    margin: 0px;
    padding: 0px;
}

/* the following pertain to the main content (i.e. the main 'metal-brushed') table */
#table_main_center {
    background: url(./images/background_table_main_center.gif) repeat-y;
}

#table_main_top {
    background: url(./images/background_table_main_top.gif) repeat-x;
}

#table_main_top_left {
    background: url(./images/rounded_corner_main_top_left.gif) no-repeat;
}

#table_main_top_right {
    background: url(./images/rounded_corner_main_top_right.gif) no-repeat;
}

#table_main_right {
    background: url(./images/background_table_main_right.gif) repeat-y;
}

#table_main_bottom {
    background: url(./images/background_table_main_bottom.gif) repeat-x;
}

#table_main_bottom_left {
    background: url(./images/rounded_corner_main_bottom_left.gif) no-repeat;
}

#table_main_bottom_right {
    background: url(./images/rounded_corner_main_bottom_right.gif) no-repeat;
}

#table_main_left {
    background: url(./images/background_table_main_left.gif) repeat-y;
}
    -->
   </style>
  </head>


<BODY>


    <center>
      <table border="0" cellpadding="0" cellspacing="0" width="904">

        <!-- TOP THIRD -->
        <tr>
          <td id="table_main_top_left">
            <img src="./images/spacer.gif" alt=" " width="23" height="23"/><br/>
          </td>
          <td width="100%" id="table_main_top">
            <img src="./images/spacer.gif" alt=" " width="1" height="1"/><br/>
          </td>
          <td id="table_main_top_right">
            <img src="./images/spacer.gif" alt=" " width="23" height="23"/><br/>
          </td>
        </tr>
        <!-- END TOP THIRD -->

        <!-- MIDDLE THIRD -->
        <tr>
          <td id="table_main_left">
            <img src="./images/spacer.gif" alt=" " width="1" height="1"/>
          </td>
          <td id="table_main_center">
            <table width="100%">
              <tr>
              <td width="96" valign="middle">
                <img src="./images/logo_no_text_shiny_96x96.gif" alt="Metavize logo" width="96" height="96"/>
              </td>
              <td style="padding: 0px 0px 0px 10px" class="page_header_title" align="left" valign="middle">
		Metavize EdgeReport
              </td>
	      <td align="right">
		<table width="100%">
		<tr><td class="page_header_alternate" align="right">
			<b>Archives</b>
		</td></tr>
		<tr><td align="right">
			> <a href="./current">View Current Report</a>
		</td></tr>
		</table>
	      </td>
              </tr>
            </table>
          </td>
          <td id="table_main_right">
            <img src="./images/spacer.gif" alt=" " width="1" height="1"/>
          </td>
        </tr>
        <!-- END MIDDLE THIRD -->

    <tr>
    <td id="table_main_left"></td>
    <td id="table_main_center" style="padding: 15px 0px 0px 0px">
	<hr width="100%" size="1" color="969696"/>

	<table width="100%" style="padding: 15px 0px 10px 0px">
        <%

	 	for(File file : directories){
			out.write("<tr><td align=\"center\">");
			out.write( "> <a href=\"./" + file.getName() + "\">" + file.getName() + "</a><br/>");
			out.write("</td></tr>");
		}

	 %>
	</table>
	<hr width="100%" size="1" color="969696"/>
    </td>
    <td id="table_main_right"></td>
    </tr>

        <!-- BOTTOM THIRD -->
        <tr>
          <td id="table_main_bottom_left">
            <img src="./images/spacer.gif" alt=" " width="23" height="23"/><br/>
          </td>
          <td id="table_main_bottom">
            <img src="./images/spacer.gif" alt=" " width="1" height="1"/><br/>
          </td>
          <td id="table_main_bottom_right">
            <img src="./images/spacer.gif" alt=" " width="23" height="23"/><br/>
          </td>
        </tr>
        <!-- END BOTTOM THIRD -->

      </table>
    </center>

</BODY>
</HTML>

