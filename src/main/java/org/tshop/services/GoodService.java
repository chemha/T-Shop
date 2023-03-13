package org.tshop.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.tshop.dto.GoodDto;
import org.tshop.dto.GoodPropertiesDto;
import org.tshop.dto.GoodSearchResultDto;
import org.tshop.entities.GoodEntity;
import org.tshop.mappers.GoodMapper;
import org.tshop.repositories.GoodRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodService {
    private final GoodRepository repository;
    private final GoodMapper mapper;

    private GoodEntity loadEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("No good found by specified ID"));
    }

    @Transactional
    public GoodDto get(final Integer id) {
        final GoodEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    @Transactional
    public GoodDto create(final GoodPropertiesDto properties) {
        final GoodEntity entity = mapper.createEntity(properties);
        return mapper.createDto(repository.save(entity));
    }

    @Transactional
    public GoodDto update(final Integer id, final GoodPropertiesDto properties) {
        final GoodEntity entity = loadEntity(id);

        mapper.updateEntity(entity, properties);
        GoodEntity updatedEntity = repository.save(entity);

        return mapper.createDto(updatedEntity);
    }

    @Transactional
    public void delete(Integer id) {
        GoodEntity entity = loadEntity(id);
        repository.delete(entity);
    }

    public GoodSearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<GoodEntity> page = repository.findAll(pageable);
        GoodSearchResultDto searchResult = new GoodSearchResultDto();
        List<GoodDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }
}
