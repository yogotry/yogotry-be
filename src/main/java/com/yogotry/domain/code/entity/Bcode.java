package com.yogotry.domain.code.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_bcode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bcode {

    @Id
    @Column(name = "bcode")
    private String bcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acode", nullable = false)
    private Acode acode;

    private String name;

    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
