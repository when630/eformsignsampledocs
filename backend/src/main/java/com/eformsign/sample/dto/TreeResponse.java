package com.eformsign.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class TreeResponse {
    private Long id;
    private String name;
    private List<TreeResponse> children = new ArrayList<>();

    public void addChild(TreeResponse child) {
        children.add(child);
    }
}