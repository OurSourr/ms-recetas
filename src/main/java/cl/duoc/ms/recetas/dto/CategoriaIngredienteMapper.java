
package cl.duoc.ms.recetas.dto;

import java.util.Objects;

import cl.duoc.ms.recetas.model.CategoriaIngrediente;

/**
 * Convierte entidades de categorías de ingredientes
 * en DTOs públicos de la API REST.
 */
public final class CategoriaIngredienteMapper {

    private CategoriaIngredienteMapper() {
        // Impide instanciar esta clase de utilidades.
    }

    /**
     * Convierte una entidad JPA en su representación pública.
     *
     * @param categoria entidad que se desea convertir
     * @return DTO con los datos públicos de la categoría
     * @throws NullPointerException si la categoría es nula
     */
    public static CategoriaIngredienteResponse toResponse(
            CategoriaIngrediente categoria) {

        Objects.requireNonNull(
                categoria,
                "La categoría no puede ser nula.");

        return new CategoriaIngredienteResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion());
    }
}
