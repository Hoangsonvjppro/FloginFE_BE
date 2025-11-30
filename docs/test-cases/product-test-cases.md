# Product - Test Cases Chi tiết

## TC_PRODUCT_001: Tạo sản phẩm mới thành công

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_PRODUCT_001 |
| **Test Name** | Tạo sản phẩm mới thành công với đầy đủ thông tin |
| **Priority** | 🔴 Critical |
| **Feature** | Product Management - Create |
| **Prerequisites** | - User đã đăng nhập<br>- Product list page đã load |
| **Test Data** | **Name**: `Laptop Dell XPS 15`<br>**Description**: `High-end laptop for developers`<br>**Price**: `35000000`<br>**Quantity**: `10` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Products page | Product list hiển thị |
| 2 | Click "Add New Product" button | Form modal opens |
| 3 | Fill Name: `Laptop Dell XPS 15` | Text entered |
| 4 | Fill Description: `High-end laptop...` | Text entered |
| 5 | Fill Price: `35000000` | Number entered |
| 6 | Fill Quantity: `10` | Number entered |
| 7 | Click "Create Product" button | Loading indicator shows |
| 8 | Verify API response | 201 Created, product object with ID returned |
| 9 | Verify UI update | Modal closes, success message shows, product appears in list |

### Expected Results
- API Response: 201 Created
- Product ID: Auto-generated (e.g., 101)
- Product in list với correct data
- Success message: "Thêm sản phẩm thành công"

### Status: [ ] Not Run [ ] Pass [ ] Fail

---

## TC_PRODUCT_002: Validation - Tên sản phẩm rỗng

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_PRODUCT_002 |
| **Test Name** | Validation error khi tên sản phẩm để trống |
| **Priority** | 🔴 Critical |
| **Feature** | Product Validation |
| **Test Data** | **Name**: _(empty)_<br>**Price**: `1000000`<br>**Quantity**: `5` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open product form | Form displayed |
| 2 | Leave Name empty | Field empty |
| 3 | Fill Price and Quantity | Filled |
| 4 | Click Submit | Validation error appears |
| 5 | Verify error message | "Tên sản phẩm không được để trống" |
| 6 | Verify API not called | No network request |

### Status: [ ] Not Run [ ] Pass [ ] Fail

---

## TC_PRODUCT_003: Cập nhật sản phẩm thành công

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_PRODUCT_003 |
| **Test Name** | Cập nhật thông tin sản phẩm |
| **Priority** | 🔴 Critical |
| **Feature** | Product Management - Update |
| **Prerequisites** | Product ID=1 tồn tại |
| **Test Data** | **Original**: `Laptop Dell` @ `15000000` VND<br>**Updated**: `Laptop Dell XPS` @ `18000000` VND |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Click Edit button on product ID=1 | Form pre-filled with current data |
| 2 | Change name to `Laptop Dell XPS` | Name updated in field |
| 3 | Change price to `18000000` | Price updated |
| 4 | Click "Update Product" | Loading indicator |
| 5 | Verify API call | PUT /api/products/1 with new data |
| 6 | Verify response | 200 OK with updated product |
| 7 | Verify UI | Product list shows new values |

### Status: [ ] Not Run [ ] Pass [ ] Fail

---

## TC_PRODUCT_004: Xóa sản phẩm thành công

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_PRODUCT_004 |
| **Test Name** | Xóa sản phẩm sau confirmation |
| **Priority** | 🔴 Critical |
| **Feature** | Product Management - Delete |
| **Prerequisites** | Product ID=1 tồn tại |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Click Delete button | Confirmation dialog appears |
| 2 | Verify dialog message | "Bạn có chắc muốn xóa...?" |
| 3 | Click "Xóa" button | Dialog closes, API called |
| 4 | Verify API call | DELETE /api/products/1 |
| 5 | Verify response | 204 No Content |
| 6 | Verify UI | Product removed from list |
| 7 | Verify success message | "Xóa sản phẩm thành công" |

### Status: [ ] Not Run [ ] Pass [ ] Fail

---

## TC_PRODUCT_005: Validation - Giá phải lớn hơn 0

| Thuộc tính | Giá trị |
|------------|---------|
| **Test Case ID** | TC_PRODUCT_005 |
| **Test Name** | Validation cho giá = 0 hoặc âm |
| **Priority** | 🔴 Critical |
| **Test Data** | **Test 1**: Price = `0`<br>**Test 2**: Price = `-100` |

### Test Steps

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open Create Product form | Form displayed |
| 2 | Fill Name: `Test Product` | Entered |
| 3 | Fill Price: `0` | Entered |
| 4 | Fill Quantity: `10` | Entered |
| 5 | Click Submit | Validation error |
| 6 | Verify error | "Giá phải lớn hơn 0" |
| 7 | Repeat với Price = `-100` | Same error |

### Status: [ ] Not Run [ ] Pass [ ] Fail

---

## Test Execution Summary

| Test Case ID | Priority | Expected | Status |
|--------------|----------|----------|--------|
| TC_PRODUCT_001 | Critical | Product created | Not Run |
| TC_PRODUCT_002 | Critical | Validation error | Not Run |
| TC_PRODUCT_003 | Critical | Product updated | Not Run |
| TC_PRODUCT_004 | Critical | Product deleted | Not Run |
| TC_PRODUCT_005 | Critical | Price validation | Not Run |

---

**Người tạo**: Nhóm FloginFE_BE  
**Ngày tạo**: 30/11/2024
