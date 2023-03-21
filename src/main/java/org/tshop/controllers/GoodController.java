package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.GoodsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.GoodDto;
import org.tshop.dto.GoodPropertiesDto;
import org.tshop.dto.GoodSearchResultDto;
import org.tshop.services.GoodService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client-addresses")
public class GoodController implements GoodsApi {
    private final GoodService service;

    @Override
    public ResponseEntity<GoodDto> createGood(GoodPropertiesDto properties) {
        GoodDto dto = service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    public ResponseEntity<Void> deleteGood(Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<GoodDto> getGood(Integer id) {
        GoodDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<GoodSearchResultDto> getGoodSearchResult(Integer pageNumber, Integer pageSize) {
        GoodSearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    public ResponseEntity<GoodDto> updateGood(Integer id, GoodPropertiesDto properties) {
        final GoodDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}
