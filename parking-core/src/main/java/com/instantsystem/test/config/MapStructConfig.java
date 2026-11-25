package com.instantsystem.test.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuration MapStruct partagée par tous les mappers du projet.
 * {@code unmappedTargetPolicy = ERROR} fait échouer la compilation si une
 * propriété cible n'est pas mappée.
 */
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapStructConfig {
}
