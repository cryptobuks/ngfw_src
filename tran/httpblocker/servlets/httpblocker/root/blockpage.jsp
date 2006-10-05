<%@ page language="java" import="com.metavize.mvvm.client.*, com.metavize.mvvm.tran.*, com.metavize.mvvm.security.*, com.metavize.tran.httpblocker.*"%>

<%
MvvmRemoteContext ctx = MvvmRemoteContextFactory.factory().systemLogin(0, Thread.currentThread().getContextClassLoader());
TransformManager tman = ctx.transformManager();

String nonce = request.getParameter("nonce");
String tidStr = request.getParameter("tid");
Tid tid = new Tid(Long.parseLong(tidStr));

TransformContext tctx = tman.transformContext(tid);
HttpBlocker tran = (HttpBlocker)tctx.transform();
BlockDetails bd = tran.getDetails(nonce);

String header = bd.getHeader();
String contact = bd.getContact();
String host = bd.getHost();
String uri = bd.getUri().toString();
String reason = bd.getReason();
String url = bd.getUrl().toString();
%>

<html>
<head>
<title>403 Forbidden</title>

<script language="JavaScript">
nonce = '<%=nonce%>';
tid = '<%=tidStr%>';
url = '<%=url%>';
</script>

</head>
<body>
<center><b><%=header%></b></center>
<p>This site blocked because of inappropriate content</p>
<p>Host: <%=host%></p>
<p>URL: <%=url%></p>
<p>Category: <%=reason%></p>

<p>Please contact <%=contact%></p>
<hr>
<address>Untangle Networks EdgeGuard</address>
</body>
</html>

<%
MvvmRemoteContextFactory.factory().logout();
%>