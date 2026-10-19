
package cl.duoc.ms.recetas.dto;

/**
 * Representa los datos públicos de una categoría de ingredientes.
 *
 * Evita exponer directamente la entidad JPA en la API REST.
 */
public record CategoriaIngredienteResponse(
        Integer idCategoria,
        String nombre,
        String descripcion) {

    public CategoriaIngredienteResponse {
        if (idCategoria == null || idCategoria <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la categoría debe ser positivo.");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio.");
        }

        nombre = nombre.trim();
    }
}
