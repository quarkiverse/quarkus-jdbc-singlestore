package io.quarkiverse.jdbc.singlestore.it.jpa;

import jakarta.persistence.Embeddable;

/**
 * Class marked @Embeddable explicitly so it is picked up.
 *
 *
 */
@SuppressWarnings("unused")
@Embeddable
public class WorkAddress {
    private String company;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
