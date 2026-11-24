package com.instantsystem.test.parking.database.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CityTest {

    @Test
    void gettersAndSetters() {
        City city = new City();
        city.setId(7L);
        city.setName("Poitiers");

        assertThat(city.getId()).isEqualTo(7L);
        assertThat(city.getName()).isEqualTo("Poitiers");
    }
}
