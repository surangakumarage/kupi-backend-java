package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Deal code info entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deal_code")
public class DealCode {

    @Id
    private String dealCode;
    private LocalDate dealDate;
    private int dealNumberForTheDay;
    @Enumerated(EnumType.STRING)
    private UserType dealType;
    private long createdAt;
}
