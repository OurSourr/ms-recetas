package cl.duoc.ms.recetas.service;

import java.util.List;

import cl.duoc.ms.recetas.model.PasoReceta;

/**
 * Contrato de operaciones de negocio para los pasos de preparación.
 */
public interface PasoRecetaService {

    /**
     * Obtiene los pasos de preparación de una receta,
     * ordenados por su número de paso.
     *
     * @param idReceta identificador de la receta
     * @return lista ordenada de pasos de preparación
     */
    List<PasoReceta> listarPasosPorReceta(Integer idReceta);
}