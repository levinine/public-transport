package com.factory.controller;

import com.factory.common.api.RestApiEndpoints;
import com.factory.common.api.RestApiRequestParams;
import com.factory.dto.RoutesDto;
import com.factory.service.StationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RestApiEndpoints.ROUTES)
public class RoutesController {

    private final StationService stationService;

    public RoutesController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public RoutesDto findRoutes(@RequestParam(RestApiRequestParams.START) String start,
                                @RequestParam(RestApiRequestParams.END) String end,
                                @RequestParam(RestApiRequestParams.DATE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {

        String[] startCoordinates = start.split(",");
        String[] endCoordinates = end.split(",");
        return stationService.search(startCoordinates, endCoordinates, date.getHour(), date.getMinute());
    }
}
