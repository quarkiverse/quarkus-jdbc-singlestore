package io.quarkiverse.jdbc.singlestore.it.jpa;

import jakarta.persistence.MappedSuperclass;

/**
 * Mapped superclass test
 *
 *
 */
@SuppressWarnings("unused")
@MappedSuperclass
public class Human extends Animal {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
