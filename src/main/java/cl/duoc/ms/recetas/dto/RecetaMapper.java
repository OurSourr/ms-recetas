package cl.duoc.ms.recetas.dto;

import java.util.Objects;

import cl.duoc.ms.recetas.model.Receta;

/**
 * Centraliza la conversión de entidades Receta
 * a DTOs utilizados en las respuestas REST.
 */
public final class RecetaMapper {

    private RecetaMapper() {
        // Evita instanciar esta clase de utilidades.
    }

    public static RecetaResponse toResponse(Receta receta) {
        Objects.requireNonNull(
                receta,
                "La receta no puede ser nula.");

        return new RecetaResponse(
                receta.getIdReceta(),
                receta.getTitulo(),
                receta.getDescripcion());
    }
}