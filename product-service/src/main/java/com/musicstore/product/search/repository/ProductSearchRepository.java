package com.musicstore.product.search.repository;

import com.musicstore.product.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    Optional<ProductDocument> findBySku(String sku);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description^2\", \"brand\"], \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"isActive\": true}}]}}")
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable);

    Page<ProductDocument> findByCategoryId(Long categoryId, Pageable pageable);

    Page<ProductDocument> findByBrand(String brand, Pageable pageable);
}