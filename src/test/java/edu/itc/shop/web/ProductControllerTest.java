
package edu.itc.shop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.itc.shop.model.ReportBuilder;
import edu.itc.shop.service.ProductNotFoundException;
import edu.itc.shop.service.ProductService;
import edu.itc.shop.service.WelcomeService;
import edu.itc.shop.web.dto.CreateProduct;
import edu.itc.shop.web.dto.ProductDto;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProductService service;

    @MockitoBean
    WelcomeService welcomeService;

    @MockitoBean
    ReportBuilder reportBuilder;

    @Test
    void listReturns200() throws Exception {
        given(service.search(null)).willReturn(List.of(
                new ProductDto(
                        1L,
                        "Mouse",
                        BigDecimal.valueOf(10.0),
                        5)));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Mouse"));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        given(service.getById(999L))
                .willThrow(
                        new ProductNotFoundException("Product 999 not found"));

        mvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void createWithInvalidBodyReturns400WithFieldErrors()
            throws Exception {

        mvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "", "price": 0, "stock": -1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void createValidProductReturns201WithLocation()
            throws Exception {

        given(service.create(any(CreateProduct.class)))
                .willReturn(
                        new ProductDto(
                                6L,
                                "USB-C cable",
                                BigDecimal.valueOf(7.90),
                                25));

        mvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "USB-C cable", "price": 7.90, "stock": 25}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/products/6"))
                .andExpect(jsonPath("$.id").value(6));
    }
}
