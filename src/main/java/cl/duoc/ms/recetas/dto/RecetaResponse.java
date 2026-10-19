package cl.duoc.ms.recetas.dto;

/**
 * DTO de respuesta para consultar recetas.
 * Evita exponer directamente las entidades JPA.
 */
public record RecetaResponse(
        Integer idReceta,
        String titulo,
        String descripcion
) {
}