package com.factory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineDto {

    private String name;
    
    private String description;
    
    private List<CoordinateDto> coordinates = new ArrayList<>();
    
    private List<CoordinateDto> stops = new ArrayList<>();
}
