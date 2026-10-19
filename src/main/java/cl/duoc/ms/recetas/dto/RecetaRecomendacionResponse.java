
package cl.duoc.ms.recetas.dto;

import java.util.List;
import java.util.Objects;

/**
 * Representa el resultado de comparar una receta
 * con los ingredientes disponibles del usuario.
 *
 * No contiene entidades JPA ni lógica de persistencia.
 */
public record RecetaRecomendacionResponse(
        Integer idReceta,
        String titulo,
        int ingredientesObligatorios,
        int ingredientesDisponibles,
        double porcentajeCompatibilidad,
        boolean puedePreparar,
        List<IngredienteFaltanteResponse> ingredientesFaltantes
) {

    public RecetaRecomendacionResponse {

        if (idReceta == null || idReceta <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la receta debe ser positivo.");
        }

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "El título de la receta es obligatorio.");
        }

        titulo = titulo.trim();

        if (ingredientesObligatorios <= 0) {
            throw new IllegalArgumentException(
                    "La receta debe tener ingredientes obligatorios.");
        }

        if (ingredientesDisponibles < 0
                || ingredientesDisponibles > ingredientesObligatorios) {
            throw new IllegalArgumentException(
                    "La cantidad de ingredientes disponibles es inválida.");
        }

        Objects.requireNonNull(
                ingredientesFaltantes,
                "La lista de ingredientes faltantes es obligatoria.");

        ingredientesFaltantes = List.copyOf(ingredientesFaltantes);

        if (ingredientesFaltantes.size()
                != ingredientesObligatorios - ingredientesDisponibles) {
            throw new IllegalArgumentException(
                    "La cantidad de ingredientes faltantes es inconsistente.");
        }

        if (!Double.isFinite(porcentajeCompatibilidad)
                || porcentajeCompatibilidad < 0
                || porcentajeCompatibilidad > 100) {
            throw new IllegalArgumentException(
                    "El porcentaje de compatibilidad es inválido.");
        }

        double porcentajeEsperado =
                ingredientesDisponibles * 100.0 / ingredientesObligatorios;

        if (Math.abs(porcentajeCompatibilidad - porcentajeEsperado) > 0.000001) {
            throw new IllegalArgumentException(
                    "El porcentaje no coincide con los ingredientes disponibles.");
        }

        if (puedePreparar
                != (ingredientesDisponibles == ingredientesObligatorios)) {
            throw new IllegalArgumentException(
                    "El estado de preparación es inconsistente.");
        }
    }
}
