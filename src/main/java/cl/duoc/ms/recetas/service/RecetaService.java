
package cl.duoc.ms.recetas.service;

import java.util.List;
import java.util.Optional;

import cl.duoc.ms.recetas.dto.RecetaDetalleResponse;
import cl.duoc.ms.recetas.model.Receta;

/**
 * Contrato de operaciones de negocio para la gestión de recetas.
 */
public interface RecetaService {

    /**
     * Obtiene las recetas publicadas y activas disponibles
     * para su consulta en la aplicación.
     *
     * @return lista de recetas publicadas y activas
     */
    List<Receta> listarRecetasPublicadas();

    /**
     * Obtiene una receta por su identificador únicamente
     * si cumple las condiciones de visibilidad pública.
     *
     * Excluye borradores, recetas inactivas y recetas
     * que incumplan las reglas de publicación.
     *
     * @param idReceta identificador de la receta
     * @return receta disponible o Optional.empty()
     */
    Optional<Receta> obtenerRecetaPublicadaPorId(Integer idReceta);

    /**
     * Obtiene el detalle público de una receta, incluyendo
     * sus pasos ordenados.
     *
     * La recuperación de datos se realiza desde el servicio,
     * respetando los límites transaccionales.
     *
     * @param idReceta identificador de la receta
     * @return detalle público o Optional.empty()
     */
    Optional<RecetaDetalleResponse> obtenerDetalleRecetaPublicada(
            Integer idReceta);

    /**
     * Publica una receta después de verificar
     * las reglas de negocio correspondientes.
     *
     * @param idReceta identificador de la receta
     * @return receta publicada
     */
    Receta publicarReceta(Integer idReceta);
}
