package com.factory.controller;

import com.factory.common.api.RestApiEndpoints;
import com.factory.dto.RefreshDto;
import com.factory.dto.RoutesDto;
import com.factory.service.StationService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RestApiEndpoints.DATA)
public class DataController {

    private final StationService stationService;

    public DataController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, value = "/refresh")
    public RoutesDto refresh(@RequestBody RefreshDto refreshDto) {
        stationService.initData();

        if (isRefreshOnly(refreshDto)) {
            return new RoutesDto();
        } else {
            String[] startCoordinates = refreshDto.getStart().split(",");
            String[] endCoordinates = refreshDto.getEnd().split(",");
            String[] time = refreshDto.getDate().split(":");
            return stationService.search(startCoordinates, endCoordinates, time);
        }
    }

    private boolean isRefreshOnly(@RequestBody RefreshDto refreshDto) {
        return StringUtils.isEmpty(refreshDto.getStart()) || StringUtils.isEmpty(refreshDto.getEnd()) || StringUtils.isEmpty(refreshDto.getDate());
    }
}
