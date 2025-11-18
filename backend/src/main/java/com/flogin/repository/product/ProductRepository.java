// filepath: E:\HK5\ktpm\Flogin\backend\src\main\java\com\flogin\repository\product\ProductRepository.java
package com.flogin.repository.product;

import com.flogin.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);
}