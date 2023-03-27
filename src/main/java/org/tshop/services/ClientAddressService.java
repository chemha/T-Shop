package org.tshop.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.tshop.dto.ClientAddressDto;
import org.tshop.dto.ClientAddressDto;
import org.tshop.dto.ClientAddressPropertiesDto;
import org.tshop.dto.ClientAddressSearchResultDto;
import org.tshop.entities.ClientAddressEntity;
import org.tshop.entities.ClientEntity;
import org.tshop.mappers.ClientAddressMapper;
import org.tshop.mappers.ClientMapper;
import org.tshop.repositories.ClientAddressRepository;
import org.tshop.repositories.ClientRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientAddressService {
    private final ClientAddressRepository repository;
    private final ClientAddressMapper mapper;
    private final ClientRepository clientRepository;

    private ClientAddressEntity loadEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("No address found by specified ID"));
    }

    public List<ClientAddressEntity> findByClientId(Integer client_id) {
        return repository.findByClientId(client_id);
    }

    public ClientAddressDto get(final Integer id) {
        final ClientAddressEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    public ClientAddressDto create(final ClientAddressPropertiesDto properties) {
        final ClientAddressEntity entity = mapper.createEntity(properties);
        entity.setClient(clientRepository.findById(properties.getClientId()).orElseThrow(() -> new RuntimeException("No client found by specified ID")));

        final ClientAddressDto dto = mapper.createDto(repository.save(entity));
        dto.setClientId(properties.getClientId());
        return dto;
    }

    @Transactional
    public ClientAddressDto update(final Integer id, final ClientAddressPropertiesDto properties) {
        final ClientAddressEntity entity = loadEntity(id);
        mapper.updateEntity(entity, properties);
        entity.setClient(clientRepository.findById(properties.getClientId()).orElseThrow(() -> new RuntimeException("No client found by specified ID")));
        ClientAddressEntity updatedEntity = repository.save(entity);
        final ClientAddressDto dto = mapper.createDto(updatedEntity);
        return dto;
    }

    @Transactional
    public void delete(Integer id) {
        ClientAddressEntity entity = loadEntity(id);
        repository.delete(entity);
    }

    public ClientAddressSearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientAddressEntity> page = repository.findAll(pageable);
        ClientAddressSearchResultDto searchResult = new ClientAddressSearchResultDto();
        List<ClientAddressDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }
}
