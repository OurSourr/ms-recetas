package cl.duoc.ms.recetas.service;

import java.util.List;

import cl.duoc.ms.recetas.model.Ingrediente;

/**
 * Contrato de operaciones de negocio para la gestión de ingredientes.
 */
public interface IngredienteService {

    /**
     * Obtiene los ingredientes que están activos.
     *
     * @return lista de ingredientes activos
     */
    List<Ingrediente> listarIngredientesActivos();
}