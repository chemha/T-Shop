package org.tshop.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.tshop.dto.OrderDto;
import org.tshop.dto.OrderPropertiesDto;
import org.tshop.dto.OrderSearchResultDto;
import org.tshop.entities.OrderEntity;
import org.tshop.mappers.OrderMapper;
import org.tshop.repositories.OrderRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;

    private OrderEntity loadEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("No product found by specified ID"));
    }

    @Transactional
    public OrderDto get(final Integer id) {
        final OrderEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    @Transactional
    public OrderDto create(final OrderPropertiesDto properties) {
        final OrderEntity entity = mapper.createEntity(properties);
        return mapper.createDto(repository.save(entity));
    }

    @Transactional
    public OrderDto update(final Integer id, final OrderPropertiesDto properties) {
        final OrderEntity entity = loadEntity(id);

        mapper.updateEntity(entity, properties);
        OrderEntity updatedEntity = repository.save(entity);

        return mapper.createDto(updatedEntity);
    }

    @Transactional
    public void delete(Integer id) {
        OrderEntity entity = loadEntity(id);
        repository.delete(entity);
    }

    public OrderSearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<OrderEntity> page = repository.findAll(pageable);
        OrderSearchResultDto searchResult = new OrderSearchResultDto();
        List<OrderDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }
}
