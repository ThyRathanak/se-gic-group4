
package edu.itc.shop.web;

import edu.itc.shop.config.ShopProperties;
import edu.itc.shop.service.ProductService;
import edu.itc.shop.web.dto.CreateProduct;
import edu.itc.shop.web.dto.ProductDto;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductPageController {

    private final ProductService productService;
    private final ShopProperties shopProperties;

    public ProductPageController(
            ProductService productService,
            ShopProperties shopProperties) {
        this.productService = productService;
        this.shopProperties = shopProperties;
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String q,
            Model model) {

        List<ProductDto> products = productService.search(q);

        List<ProductRow> rows = products.stream()
                .map(p -> new ProductRow(
                        p.id(),
                        p.name(),
                        p.price(),
                        p.price().multiply(
                                BigDecimal.ONE.add(shopProperties.taxRate())),
                        p.stock()))
                .toList();

        model.addAttribute("products", rows);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("currency", shopProperties.currency());
        model.addAttribute("threshold",
                shopProperties.lowStockThreshold());

        return "products/list";
    }

    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new CreateProduct("", null, 0));
        return "products/form";
    }

    @PostMapping("/products")
    public String createProduct(
            @Valid @ModelAttribute("product") CreateProduct product,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "products/form";
        }

        productService.create(product);

        redirectAttributes.addFlashAttribute(
                "message", "Product created successfully!");

        return "redirect:/products";
    }

    public record ProductRow(
            long id,
            String name,
            BigDecimal price,
            BigDecimal gross,
            int stock) {
    }
}