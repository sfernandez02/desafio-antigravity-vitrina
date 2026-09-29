package com.nttdata.vitrina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, length = 512)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String format;

    @Column(length = 512)
    private String category;

    @Column(name = "category_group", length = 255)
    private String categoryGroup;

    private Long price;

    @Column(name = "price_unit", length = 64)
    private String priceUnit;

    @Column(name = "original_price")
    private Long originalPrice;

    @Column(length = 10)
    private String currency;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "product_url", length = 1024)
    private String productUrl;

    @Column(name = "extracted_at", length = 64)
    private String extractedAt;

    public Product() {
    }

    public Product(String id, String name, String description, String format, String category,
                   String categoryGroup, Long price, String priceUnit, Long originalPrice,
                   String currency, String imageUrl, String productUrl, String extractedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.format = format;
        this.category = category;
        this.categoryGroup = categoryGroup;
        this.price = price;
        this.priceUnit = priceUnit;
        this.originalPrice = originalPrice;
        this.currency = currency;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.extractedAt = extractedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryGroup() {
        return categoryGroup;
    }

    public void setCategoryGroup(String categoryGroup) {
        this.categoryGroup = categoryGroup;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public Long getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Long originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(String extractedAt) {
        this.extractedAt = extractedAt;
    }
}
