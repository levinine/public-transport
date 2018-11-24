package com.factory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {

    private String name;

    private String lat;

    private String lon;

    private List<String> lines;
}
