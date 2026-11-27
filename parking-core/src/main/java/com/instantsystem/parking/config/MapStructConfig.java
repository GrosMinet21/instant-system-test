package com.instantsystem.parking.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct configuration for all mappers in the project.
 * {@code unmappedTargetPolicy = ERROR} causes compilation to fail if a
 * target property is not mapped.
 */
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapStructConfig {
}
