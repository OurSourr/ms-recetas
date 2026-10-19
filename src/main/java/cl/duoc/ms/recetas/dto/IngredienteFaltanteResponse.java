
package cl.duoc.ms.recetas.dto;

/**
 * Representa un ingrediente obligatorio que el usuario
 * necesita para preparar una receta.
 *
 * Es un DTO de respuesta, independiente de las entidades JPA.
 */
public record IngredienteFaltanteResponse(
        Integer idIngrediente,
        String nombre
) {

    public IngredienteFaltanteResponse {

        if (idIngrediente == null || idIngrediente <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del ingrediente debe ser positivo.");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del ingrediente es obligatorio.");
        }

        nombre = nombre.trim();
    }
}
