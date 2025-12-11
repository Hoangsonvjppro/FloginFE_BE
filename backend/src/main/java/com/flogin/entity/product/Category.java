package com.flogin.entity.product;

/**
 * Category enum cho Product
 * Theo yêu cầu assignment: Category phải thuộc danh sách có sẵn
 */
public enum Category {
    ELECTRONICS("Electronics", "Điện tử"),
    CLOTHING("Clothing", "Thời trang"),
    FOOD("Food", "Thực phẩm"),
    BOOKS("Books", "Sách"),
    SPORTS("Sports", "Thể thao"),
    HOME("Home", "Gia dụng"),
    OTHER("Other", "Khác");

    private final String displayName;
    private final String vietnameseName;

    Category(String displayName, String vietnameseName) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    /**
     * Kiểm tra xem một string có phải là category hợp lệ không
     */
    public static boolean isValid(String category) {
        if (category == null) {
            return false;
        }
        try {
            Category.valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parse string thành Category, throw exception nếu không hợp lệ
     */
    public static Category fromString(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category + 
                ". Valid categories are: ELECTRONICS, CLOTHING, FOOD, BOOKS, SPORTS, HOME, OTHER");
        }
    }
}
