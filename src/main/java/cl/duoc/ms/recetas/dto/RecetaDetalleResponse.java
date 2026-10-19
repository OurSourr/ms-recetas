package cl.duoc.ms.recetas.dto;

import java.util.List;

/**
 * DTO de respuesta para consultar el detalle
 * de una receta publicada.
 *
 * Mantiene separados los datos públicos
 * de las entidades de persistencia JPA.
 */
public record RecetaDetalleResponse(
        Integer idReceta,
        String titulo,
        String descripcion,
        List<PasoRecetaResponse> pasos
) {

    /**
     * Protege la lista de modificaciones externas
     * y evita recibir una colección nula.
     */
    public RecetaDetalleResponse {
        pasos = List.copyOf(pasos);
    }
}