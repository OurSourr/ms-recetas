package cl.duoc.ms.recetas.dto;

/**
 * DTO que representa un paso de preparación
 * de una receta en las respuestas de la API REST.
 */
public record PasoRecetaResponse(
        Integer numeroPaso,
        String instruccion
) {
}