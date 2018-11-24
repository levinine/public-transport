package com.factory.controller;

import com.factory.common.api.RestApiEndpoints;
import com.factory.dto.LineDto;
import com.factory.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RestApiEndpoints.LINES)
public class LinesController {

    private final StationService stationService;

    public LinesController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<LineDto> findLines() {
        return stationService.findAllLines();
    }
}
