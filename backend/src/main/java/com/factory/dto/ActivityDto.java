package com.factory.dto;

import com.factory.common.api.RestApiConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityDto {

    @JsonProperty(RestApiConstants.TYPE)
    private int type;

    @JsonProperty(RestApiConstants.START_COORDINATE)
    private CoordinateDto startCoordinate;

    @JsonProperty(RestApiConstants.END_COORDINATE)
    private CoordinateDto endCoordinate;

    @JsonProperty(RestApiConstants.PATH_COORDINATES)
    private List<CoordinateDto> pathCoordinates;

    @JsonProperty(RestApiConstants.ESTIMATED_TIME)
    private double estimatedTime;

    @JsonProperty(RestApiConstants.START_STATION)
    private String startStation;

    @JsonProperty(RestApiConstants.END_STATION)
    private String endStation;

    @JsonProperty(RestApiConstants.BUS_NUMBER)
    private String line;

    @JsonProperty(RestApiConstants.NUMBER_OF_STATIONS)
    private Integer numberOfStations;

    @JsonProperty(RestApiConstants.PREDICTED_DEPARTURE_TIMES)
    private List<String> predictedDepartureTimes;
}
