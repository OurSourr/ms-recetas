package cl.duoc.ms.recetas.service;

import java.util.List;

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
}