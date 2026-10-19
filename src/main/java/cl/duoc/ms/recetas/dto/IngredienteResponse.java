package cl.duoc.ms.recetas.dto;

/**
 * Representa la información pública de un ingrediente.
 *
 * Evita exponer directamente las entidades JPA
 * a los consumidores de la API.
 */
public record IngredienteResponse(
        Integer idIngrediente,
        String nombre,
        String descripcion) {

    public IngredienteResponse {

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