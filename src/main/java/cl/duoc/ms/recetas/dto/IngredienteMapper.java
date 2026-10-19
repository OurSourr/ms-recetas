package cl.duoc.ms.recetas.dto;

import java.util.Objects;

import cl.duoc.ms.recetas.model.Ingrediente;

/**
 * Centraliza la conversión de entidades Ingrediente
 * a DTOs utilizados en las respuestas REST.
 */
public final class IngredienteMapper {

    private IngredienteMapper() {
        // Evita instanciar esta clase de utilidades.
    }

    /**
     * Convierte una entidad Ingrediente a su DTO público.
     *
     * @param ingrediente entidad que se desea convertir
     * @return representación pública del ingrediente
     */
    public static IngredienteResponse toResponse(Ingrediente ingrediente) {

        Objects.requireNonNull(
                ingrediente,
                "El ingrediente no puede ser nulo.");

        return new IngredienteResponse(
                ingrediente.getIdIngrediente(),
                ingrediente.getNombre(),
                ingrediente.getDescripcion());
    }
}