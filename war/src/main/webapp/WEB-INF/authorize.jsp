<%--

    Aipo is a groupware program developed by Aimluck,Inc.
    Copyright (C) 2004-2011 Aimluck,Inc.
    http://www.aipo.com/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>

<%@ page import="net.oauth.OAuthConsumer" %>
<%@ page import="org.apache.shindig.social.opensocial.oauth.OAuthEntry" %>
<%@ page import="org.apache.shindig.social.opensocial.oauth.OAuthDataStore" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%
  // Gather data passed in to us.
  OAuthConsumer consumer = (OAuthConsumer)request.getAttribute("CONSUMER");
  OAuthEntry entry = (OAuthEntry) request.getAttribute("OAUTH_ENTRY");
  OAuthDataStore dataStore = (OAuthDataStore) request.getAttribute("OAUTH_DATASTORE");
  String token = (String)request.getAttribute("TOKEN");
  String callback = (String)request.getAttribute("CALLBACK");

  // Check if the user already authorized
  // TODO - this is a bit hard since we cannot get at the jsondb here...

  // If user clicked on the Authorize button then we're good.
  if (request.getParameter("Authorize") != null) {
    // If the user clicked the Authorize button we authorize the token and redirect back.
    dataStore.authorizeToken(entry, SecurityUtils.getSubject().getPrincipal().toString());

    // Bounce back to the servlet to handle redirecting to the callback URL
    request.getRequestDispatcher("/oauth/authorize?oauth_token=" + token + "&oauth_callback=" + callback)
            .forward(request,response);
  } else if (request.getParameter("Deny") != null) {
    dataStore.removeToken(entry);
  }
  // Gather some data
  pageContext.setAttribute("appTitle", consumer.getProperty("title") , PageContext.PAGE_SCOPE);
  pageContext.setAttribute("appDesc", consumer.getProperty("description"), PageContext.PAGE_SCOPE);
    
  pageContext.setAttribute("appIcon", consumer.getProperty("icon"));
  pageContext.setAttribute("appThumbnail", consumer.getProperty("thumbnail"));
%>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Your Friendly OAuth Provider</title>
</head>
<body>
Greetings <shiro:principal/>,<br/><br/>

The following application wants to access your account information<br/><br/>

<h3><img src="${appIcon}"/> <b><c:out value="${appTitle}"/></b> is trying to access your information.</h3>
<img src="${appThumbnail}" align="left" width="120" height="60"/>
<c:out value="${appDesc}" default=""/>
<br/>

<form name="authZForm" action="authorize" method="POST">
  <input type="hidden" name="oauth_token" value="<%= token %>"/>
  <input type="submit" name="Authorize" value="Deny"/>
  <input type="submit" name="Authorize" value="Authorize"/>
</form>

</body>
</html>
