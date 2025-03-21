package io.quarkiverse.jdbc.singlestore.it.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class SequencedAddress {

    private long id;
    private String street;

    public SequencedAddress() {
    }

    public SequencedAddress(String street) {
        this.street = street;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "addressSeq")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String name) {
        this.street = name;
    }

    public void describeFully(StringBuilder sb) {
        sb.append("Address with id=").append(id).append(", street='").append(street).append("'");
    }
}
