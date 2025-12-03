package com.todak.api.health;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TestEntity {

    @Id
    private Long id;

    private String name;

    public TestEntity() {}

    public TestEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
