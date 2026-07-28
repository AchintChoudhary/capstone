package com.capstone.scheduler.dto;

public class CategoryDto {
    private Long id;
    private String name;
    private String color;

    public CategoryDto() {}

    public CategoryDto(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public static CategoryDtoBuilder builder() {
        return new CategoryDtoBuilder();
    }

    public static class CategoryDtoBuilder {
        private Long id;
        private String name;
        private String color;

        public CategoryDtoBuilder id(Long id) { this.id = id; return this; }
        public CategoryDtoBuilder name(String name) { this.name = name; return this; }
        public CategoryDtoBuilder color(String color) { this.color = color; return this; }

        public CategoryDto build() {
            return new CategoryDto(id, name, color);
        }
    }
}
