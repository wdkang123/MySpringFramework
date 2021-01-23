<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="BASE" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>用户信息</title>
</head>
<body>
<h1>用户信息</h1>
<table>
    <tr>
        <th>username</th>
        <th>password</th>
    </tr>
    <c:forEach var="userinfo" items="${userList}">
        <tr>
            <td>${userinfo.username}</td>
            <td>${userinfo.password}</td>
            <td>
                <a href="#">详情</a>
                <a href="#">编辑</a>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>