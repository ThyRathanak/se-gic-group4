
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Add Product</title>
    <link rel="stylesheet" href="/css/site.css">
</head>
<body>

<h1>Add Product</h1>

<form:form modelAttribute="product"
           method="post"
           action="${pageContext.request.contextPath}/products">

    <div>
        <label for="name">Product name</label>
        <form:input path="name" id="name"/>
        <form:errors path="name" cssClass="error"/>
    </div>

    <div>
        <label for="price">Price</label>
        <form:input path="price" id="price" type="number"
                    step="0.01" min="0.01"/>
        <form:errors path="price" cssClass="error"/>
    </div>

    <div>
        <label for="stock">Stock</label>
        <form:input path="stock" id="stock" type="number" min="0"/>
        <form:errors path="stock" cssClass="error"/>
    </div>

    <p>
        <button type="submit">Create Product</button>
        <a href="${pageContext.request.contextPath}/products">Cancel</a>
    </p>

</form:form>

</body>
</html>