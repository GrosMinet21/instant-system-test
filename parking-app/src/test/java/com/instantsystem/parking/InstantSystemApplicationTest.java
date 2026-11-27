package com.instantsystem.parking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

// Scheduling disabled: the test must not call the real Poitiers API.
@SpringBootTest(properties = {"parking.sync.enabled=false", "embedded.postgres.port=0"})
class InstantSystemApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }
}
