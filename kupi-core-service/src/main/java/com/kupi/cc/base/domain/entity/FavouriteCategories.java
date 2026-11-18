package com.kupi.cc.base.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Favourite Categories
 */
@Getter
@Setter
@Entity
@Table(name = "favourite_categories")
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteCategories {

    @Id
    private String userId;
    @ElementCollection
    private Set<String> categoryIds = new HashSet<>();

}
