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
import org.tshop.repositories.ClientAddressRepository;
import org.tshop.repositories.ClientRepository;
import org.tshop.repositories.OrderRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ClientAddressRepository clientAddressRepository;

    private final OrderMapper mapper;

    public OrderEntity loadEntity(Integer id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("No order found by specified ID"));
    }

    @Transactional
    public OrderDto get(final Integer id) {
        final OrderEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    @Transactional
    public OrderDto create(final OrderPropertiesDto properties) {
        final OrderEntity entity = mapper.createEntity(properties);
        entity.setClient(clientRepository.findById(properties.getClientId()).orElseThrow(() -> new RuntimeException("No client found by specified ID")));
        entity.setAddress(clientAddressRepository.findById(properties.getAddressId()).orElseThrow(() -> new RuntimeException("No address found by specified ID")));

        final OrderDto dto = mapper.createDto(orderRepository.save(entity));
        dto.setClientId(properties.getClientId());
        dto.setAddressId(properties.getAddressId());

        return dto;
    }

    @Transactional
    public OrderDto update(final Integer id, final OrderPropertiesDto properties) {
        final OrderEntity entity = loadEntity(id);

        mapper.updateEntity(entity, properties);
        OrderEntity updatedEntity = orderRepository.save(entity);

        return mapper.createDto(updatedEntity);
    }

    @Transactional
    public void delete(Integer id) {
        OrderEntity entity = loadEntity(id);
        orderRepository.delete(entity);
    }

    public OrderSearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<OrderEntity> page = orderRepository.findAll(pageable);
        OrderSearchResultDto searchResult = new OrderSearchResultDto();
        List<OrderDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }

    public List<OrderEntity> findAll() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> findByClientId(Integer id) {
        return orderRepository.findByClientId(id);
    }
}
