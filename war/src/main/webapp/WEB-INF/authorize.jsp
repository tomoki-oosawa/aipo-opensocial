<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="org.apache.shindig.social.core.oauth2.OAuth2Client" %>
<%@ page import="org.apache.shindig.social.core.oauth2.OAuth2NormalizedRequest" %>
<% OAuth2Client oauth2Client = (OAuth2Client)request.getAttribute("com.aipo.OAuth2Client"); %>
<% OAuth2NormalizedRequest oauth2Request = (OAuth2NormalizedRequest)request.getAttribute("com.aipo.OAuth2NormalizedRequest"); %>
<%

request.setAttribute("oauth2Client", oauth2Client);
request.setAttribute("oauth2Request", oauth2Request);
String title = oauth2Client.getTitle();
String clientId = oauth2Request.getClientId();
String redirectUri = oauth2Request.getRedirectURI();
request.setAttribute("title", title);
request.setAttribute("clientId", clientId);
request.setAttribute("redirectUri", redirectUri);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>アプリ連携の開始： <c:out value="${title}"/></title>
</head>
<body>
<h2>アプリ連携の開始： <c:out value="${title}"/></h2>
このアプリ連携を許可しますか？連携を許可すると、連携アプリはあなたのAipo.comアカウントにアクセスできます。<br />
<br />
このアプリは以下のアクセス権を要求しています。
<ul>
<li>データの読み込み</li>
<li>データの書き込み</li>
</ul>

<form name="authZForm" action="authorize" method="POST">
  <input id="client_id" name="client_id" type="hidden" value='<c:out value="${clientId}"/>' />
  <input id="redirect_uri" name="redirect_uri" type="hidden" value='<c:out value="${redirectUri}"/>' />
  <input id="state" name="state" type="hidden" />
  <input id="response_type" name="response_type" type="hidden" value="code" />
  <input id="scope" name="scope" type="hidden" value="r_all w_all" />
  <input type="submit" name="authorize" value="許可する"/>
  <input type="button" name="authorize" onclick="history.back();return false" value="拒否する"/>
</form>

</body>
</html>