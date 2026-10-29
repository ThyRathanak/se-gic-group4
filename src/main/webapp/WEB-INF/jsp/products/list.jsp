 <%-- src/main/webapp/WEB-INF/jsp/products/list.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Products</title>
    <link rel="stylesheet" href="/css/site.css">
</head>
<body>

<h1>Products (${currency})</h1>

<c:if test="${not empty message}">
    <p class="flash"><c:out value="${message}"/></p>
</c:if>

<form method="get" action="/products">
    <input name="q"
           value="<c:out value='${q}'/>"
           placeholder="Search products">
    <button type="submit">Go</button>
    <a href="/products">Clear</a>
</form>

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Net</th>
            <th>Gross</th>
            <th>Stock</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="p" items="${products}">
            <tr class="${p.stock < threshold ? 'low' : ''}">
                <td><c:out value="${p.name}"/></td>
                <td>
                    <c:out value="${currency}"/>
                    <fmt:formatNumber value="${p.price}"
                                      minFractionDigits="2"
                                      maxFractionDigits="2"/>
                </td>
                <td>
                    <c:out value="${currency}"/>
                    <fmt:formatNumber value="${p.gross}"
                                      minFractionDigits="2"
                                      maxFractionDigits="2"/>
                </td>
                <td><c:out value="${p.stock}"/></td>
            </tr>
        </c:forEach>

        <c:if test="${empty products}">
            <tr>
                <td colspan="4">No products found.</td>
            </tr>
        </c:if>
    </tbody>
</table>

<p>
    <a href="/products/new">Add product</a>
</p>

</body>
</html>