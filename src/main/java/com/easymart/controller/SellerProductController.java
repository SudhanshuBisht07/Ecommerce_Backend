package com.easymart.controller;

import com.easymart.exceptions.ProductException;
import com.easymart.exceptions.SellerException;
import com.easymart.model.Product;
import com.easymart.model.Seller;
import com.easymart.request.CreateProductRequest;
import com.easymart.response.ImageUploadResponse;
import com.easymart.service.CloudinaryService;
import com.easymart.service.ProductService;
import com.easymart.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/products")
public class SellerProductController {

    private final ProductService productService;
    private final SellerService sellerService;
    private final CloudinaryService cloudinaryService;

    @GetMapping()
    public ResponseEntity<List<Product>> getProductBySellerId(@RequestHeader("Authorization") String jwt)throws ProductException, SellerException{
        Seller seller=sellerService.getSellerProfile(jwt);
        List<Product> products=productService.getProductBySellerId(seller.getId());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request, @RequestHeader("Authorization") String jwt)throws ProductException, SellerException{
        Seller seller=sellerService.getSellerProfile(jwt);
        Product product=productService.createProduct(request, seller);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String jwt) throws SellerException {
        sellerService.getSellerProfile(jwt); // confirms the caller is a real, logged-in seller

        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return new ResponseEntity<>("Only image files are allowed", HttpStatus.BAD_REQUEST);
        }

        try {
            String url = cloudinaryService.uploadImage(file, "products");
            return new ResponseEntity<>(new ImageUploadResponse(url), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>("Could not upload image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @RequestHeader("Authorization") String jwt){
        try{
            Seller seller = sellerService.getSellerProfile(jwt);
            Product product = productService.findProductById(productId);
            if(!product.getSeller().getId().equals(seller.getId())){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            productService.deleteProduct(productId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(ProductException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch(SellerException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @RequestBody CreateProductRequest product,@RequestHeader("Authorization") String jwt){
        try{
            Seller seller = sellerService.getSellerProfile(jwt);
            Product existingProduct = productService.findProductById(productId);
            if(!existingProduct.getSeller().getId().equals(seller.getId())){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            Product updatedProduct = productService.updateProduct(productId, product);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        }catch(ProductException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch(SellerException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

}
