package com.instantsystem.test.parking.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineUtilTest {

    @Test
    void distanceMeters_samePoint_isZero() {
        assertThat(HaversineUtil.distanceMeters(46.58, 0.34, 46.58, 0.34)).isEqualTo(0.0);
    }

    @Test
    void distanceMeters_knownPoints_isAccurate() {
        // Paris → Poitiers ≈ 294 km (distance à vol d'oiseau)
        double distance = HaversineUtil.distanceMeters(48.8566, 2.3522, 46.5802, 0.3404);
        assertThat(distance).isBetween(290_000.0, 300_000.0);
    }

    @Test
    void distanceMeters_isSymmetric() {
        double ab = HaversineUtil.distanceMeters(48.8566, 2.3522, 46.5802, 0.3404);
        double ba = HaversineUtil.distanceMeters(46.5802, 0.3404, 48.8566, 2.3522);
        assertThat(ab).isCloseTo(ba, org.assertj.core.data.Offset.offset(0.001));
    }
}
