package com.factory.dto;

import com.factory.common.api.RestApiConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {

    @JsonProperty(RestApiConstants.ESTIMATED_TIME)
    private double estimatedTime;

    @JsonProperty(RestApiConstants.ACTIVITIES)
    private List<ActivityDto> activities = new ArrayList<>();
}
