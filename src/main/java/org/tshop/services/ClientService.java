package org.tshop.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.entities.ClientEntity;
import org.tshop.entities.ClientAddressEntity;
import org.tshop.mappers.ClientMapper;
import org.tshop.repositories.AddressRepository;
import org.tshop.repositories.ClientRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final ClientMapper mapper;

    private ClientEntity loadEntity(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new RuntimeException("No client found by specified ID"));
    }

    @Transactional
    public ClientDto get(final Long id) {
        final ClientEntity entity = loadEntity(id);
        return mapper.createDto(entity);
    }

    @Transactional
    public ClientDto create(final ClientPropertiesDto properties) {
        final ClientEntity entity = mapper.createEntity(properties);
        return mapper.createDto(clientRepository.save(entity));
    }

    @Transactional
    public ClientDto update(final Long id, final ClientPropertiesDto properties) {
        final ClientEntity entity = loadEntity(id);

        mapper.updateEntity(entity, properties);
        ClientEntity updatedEntity = clientRepository.save(entity);

        return mapper.createDto(updatedEntity);
    }

    @Transactional
    public void delete(Long id) {
        ClientEntity entity = loadEntity(id);
        clientRepository.delete(entity);
    }

    @Transactional
    public Collection<ClientAddressEntity> findAllClientAddress(final Long id) {
        final Optional<ClientEntity> clientById = clientRepository.findClientById(id);
        if (clientById.isPresent()) {
            Hibernate.initialize(clientById.get().getClientAddressEntities());
            return clientById.get().getClientAddressEntities();
        } else {
            return Collections.emptyList();
        }
    }

    public ClientSearchResultDto getSearchResult(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<ClientEntity> page = clientRepository.findAll(pageable);
        ClientSearchResultDto searchResult = new ClientSearchResultDto();
        List<ClientDto> content = page.getContent().stream().map(mapper::createDto).toList();
        searchResult.setContent(content);
        searchResult.setPageCount(page.getTotalPages());
        searchResult.setPageNumber(pageNumber);
        searchResult.setPageSize(pageSize);
        return searchResult;
    }
}
