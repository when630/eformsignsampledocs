// CategoryClosureId.java
package com.eformsign.sample.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryClosureId implements Serializable {
    private Long ancestor;
    private Long descendant;
}