package com.nttdata.vitrina.dto;

public class ProductSummaryDto {

    private String id;
    private String name;
    private String category;
    private String categoryGroup;
    private String format;
    private Long price;
    private String priceUnit;
    private Long originalPrice;
    private String currency;
    private String imageUrl;
    private String productUrl;

    public ProductSummaryDto() {
    }

    public ProductSummaryDto(String id, String name, String category, String categoryGroup,
                             String format, Long price, String priceUnit, Long originalPrice,
                             String currency, String imageUrl, String productUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.categoryGroup = categoryGroup;
        this.format = format;
        this.price = price;
        this.priceUnit = priceUnit;
        this.originalPrice = originalPrice;
        this.currency = currency;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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
}
