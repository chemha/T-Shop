package org.tshop.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.tshop.dto.ProductDto;
import org.tshop.dto.ProductPropertiesDto;
import org.tshop.dto.ProductSearchResultDto;
import org.tshop.dto.ProductDto;
import org.tshop.entities.ClientAddressEntity;
import org.tshop.entities.ProductEntity;
import org.tshop.mappers.ClientMapper;
import org.tshop.mappers.ProductMapper;
import org.tshop.repositories.CategoryRepository;
import org.tshop.repositories.ProductRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    public ProductEntity loadEntity(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("No product found by specified ID"));
    }

    @Transactional
    public ProductDto get(final Integer id) {
        final ProductEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    @Transactional
    public ProductDto create(final ProductPropertiesDto properties) {
        final ProductEntity entity = mapper.createEntity(properties);
        entity.setCategory(categoryRepository.findById(properties.getCategoryId()).orElseThrow(() -> new RuntimeException("No category found by specified ID")));
        return mapper.createDto(productRepository.save(entity));
    }

    @Transactional
    public ProductDto update(final Integer id, final ProductPropertiesDto properties) {
        final ProductEntity entity = loadEntity(id);
        mapper.updateEntity(entity, properties);
        entity.setCategory(categoryRepository.findById(properties.getCategoryId()).orElseThrow(() -> new RuntimeException("No category found by specified ID")));
        ProductEntity updatedEntity = productRepository.save(entity);

        return mapper.createDto(updatedEntity);
    }

    @Transactional
    public void delete(Integer id) {
        ProductEntity entity = loadEntity(id);
        productRepository.delete(entity);
    }

    public ProductSearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<ProductEntity> page = productRepository.findAll(pageable);
        ProductSearchResultDto searchResult = new ProductSearchResultDto();
        List<ProductDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }

    public List<ProductEntity> findAll() {
        return productRepository.findAll();
    }
}
