package com.factory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Line {

    private String name;

    private String description;

    private List<Coordinate> coordinates;

    private List<String> timeTable;
}
