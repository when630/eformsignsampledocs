package com.eformsign.sample.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CategoryClosureId.class)
public class CategoryClosure {
    @Id
    @Column(name = "ancestor_id")
    private Long ancestor;

    @Id
    @Column(name = "descendant_id")
    private Long descendant;

    private int depth;
}