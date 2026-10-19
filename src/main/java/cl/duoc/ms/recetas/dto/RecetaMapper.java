package cl.duoc.ms.recetas.dto;

import java.util.List;
import java.util.Objects;

import cl.duoc.ms.recetas.model.PasoReceta;
import cl.duoc.ms.recetas.model.Receta;

/**
 * Centraliza la conversión de entidades JPA
 * a DTOs utilizados en las respuestas REST.
 */
public final class RecetaMapper {

    private RecetaMapper() {
        // Evita instanciar esta clase de utilidades.
    }

    /**
     * Convierte una receta a su representación resumida.
     */
    public static RecetaResponse toResponse(Receta receta) {
        Objects.requireNonNull(
                receta,
                "La receta no puede ser nula.");

        return new RecetaResponse(
                receta.getIdReceta(),
                receta.getTitulo(),
                receta.getDescripcion());
    }

    /**
     * Convierte un paso de preparación a su DTO.
     */
    public static PasoRecetaResponse toPasoResponse(PasoReceta paso) {
        Objects.requireNonNull(
                paso,
                "El paso de preparación no puede ser nulo.");

        return new PasoRecetaResponse(
                paso.getNumeroPaso(),
                paso.getInstruccion());
    }

    /**
     * Convierte una receta y sus pasos a un DTO de detalle.
     *
     * Los pasos deben llegar ordenados desde el repositorio.
     */
    public static RecetaDetalleResponse toDetalleResponse(
            Receta receta,
            List<PasoReceta> pasos) {

        Objects.requireNonNull(
                receta,
                "La receta no puede ser nula.");

        Objects.requireNonNull(
                pasos,
                "La lista de pasos no puede ser nula.");

        List<PasoRecetaResponse> pasosResponse = pasos.stream()
                .map(RecetaMapper::toPasoResponse)
                .toList();

        return new RecetaDetalleResponse(
                receta.getIdReceta(),
                receta.getTitulo(),
                receta.getDescripcion(),
                pasosResponse);
    }
}