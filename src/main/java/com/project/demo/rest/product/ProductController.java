package com.project.demo.rest.product;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;
import javax.xml.catalog.Catalog;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public  ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber() + 1);
        meta.setPageSize(productPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products retrieved successfully",
                productPage.getContent(), HttpStatus.OK, meta);
    }

   /* public List<Product> getAllCategories(){
        return productRepository.findAll();
    }*/

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")

    public Product updateProduct(@PathVariable Long id, @RequestBody Product newProduct) {
        Optional<Category> optionalCategory = categoryRepository.findById(newProduct.getCategory().getId());
        if (optionalCategory.isEmpty()) {
            return null;
        }
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(newProduct.getName());
                    existingProduct.setDescription(newProduct.getDescription());
                    existingProduct.setPrice(newProduct.getPrice());
                    existingProduct.setOnStock(newProduct.getOnStock());
                    existingProduct.setCategory(optionalCategory.get());
                    return productRepository.save(existingProduct);
                })
                .orElseGet(() -> {
                    newProduct.setId(id);
                    return productRepository.save(newProduct);
                });
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Product addProduct(@RequestBody Product newProduct) {
        Optional<Category> optionalCategory = categoryRepository.findById(newProduct.getCategory().getId());
        if (optionalCategory.isEmpty()) {
            return null;
        }
        var product = new Product();
        product.setName(newProduct.getName());
        product.setDescription(newProduct.getDescription());
        product.setPrice(newProduct.getPrice());
        product.setOnStock(newProduct.getOnStock());
        product.setCategory(optionalCategory.get());
        return  productRepository.save(product);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProduct (@PathVariable Long id) {
        productRepository.deleteById(id);
    }

}
