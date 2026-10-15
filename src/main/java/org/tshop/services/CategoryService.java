package org.tshop.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.tshop.dto.CategoryDto;
import org.tshop.dto.CategoryPropertiesDto;
import org.tshop.dto.CategorySearchResultDto;
import org.tshop.entities.CategoryEntity;
import org.tshop.entities.ProductEntity;
import org.tshop.mappers.CategoryMapper;
import org.tshop.repositories.CategoryRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryEntity loadEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("No category found by specified ID"));
    }

    @Transactional
    public CategoryDto get(final Integer id) {
        final CategoryEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    @Transactional
    public CategoryDto create(final CategoryPropertiesDto properties) {
        final CategoryEntity entity = mapper.createEntity(properties);
        return mapper.createDto(repository.save(entity));
    }

    @Transactional
    public CategoryDto update(final Integer id, final CategoryPropertiesDto properties) {
        final CategoryEntity entity = loadEntity(id);

        mapper.updateEntity(entity, properties);
        CategoryEntity updatedEntity = repository.save(entity);

        return mapper.createDto(updatedEntity);
    }

    @Transactional
    public void delete(Integer id) {
        CategoryEntity entity = loadEntity(id);
        repository.delete(entity);
    }

    public CategorySearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<CategoryEntity> page = repository.findAll(pageable);
        CategorySearchResultDto searchResult = new CategorySearchResultDto();
        List<CategoryDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }

    public List<CategoryEntity> findAll() {
        return repository.findAll();
    }
}
