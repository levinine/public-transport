package com.factory.controller;

import com.factory.common.api.RestApiEndpoints;
import com.factory.common.api.RestApiRequestParams;
import com.factory.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RestApiEndpoints.ROUTES)
public class RoutesController {

    private final StationService stationService;

    public RoutesController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public String findRoutes(@RequestParam(RestApiRequestParams.START) String start,
                             @RequestParam(RestApiRequestParams.END) String end,
                             @RequestParam(RestApiRequestParams.DATE) String date) {
        String[] startCoordinates = start.split(",");
        String[] endCoordinates = end.split(",");
        String[] time = date.split(":");
        stationService.search(startCoordinates, endCoordinates, time);
        return "It's working";
    }
}
