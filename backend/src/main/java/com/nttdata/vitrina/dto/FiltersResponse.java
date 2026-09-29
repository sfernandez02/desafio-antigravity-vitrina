package com.nttdata.vitrina.dto;

import java.util.List;

public class FiltersResponse {

    private List<String> categories;
    private List<String> formats;

    public FiltersResponse() {
    }

    public FiltersResponse(List<String> categories, List<String> formats) {
        this.categories = categories;
        this.formats = formats;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
}
