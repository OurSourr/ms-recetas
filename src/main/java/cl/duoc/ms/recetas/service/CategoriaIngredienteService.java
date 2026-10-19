package cl.duoc.ms.recetas.service;

import java.util.List;

import cl.duoc.ms.recetas.model.CategoriaIngrediente;

/**
 * Contrato de operaciones de negocio para las categorías
 * de ingredientes.
 */
public interface CategoriaIngredienteService {

    /**
     * Obtiene las categorías que están activas.
     *
     * @return lista de categorías activas
     */
    List<CategoriaIngrediente> listarCategoriasActivas();
}