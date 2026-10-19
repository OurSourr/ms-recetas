
package cl.duoc.ms.recetas.service;

import java.util.List;
import java.util.Set;

import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;

/**
 * Define las operaciones de negocio para recomendar recetas
 * según los ingredientes disponibles del usuario.
 */
public interface DescubrimientoService {

    /**
     * Obtiene recomendaciones ordenadas por compatibilidad.
     *
     * @param idsIngredientesDisponibles ingredientes del usuario
     * @return lista de recetas recomendadas
     */
    List<RecetaRecomendacionResponse> recomendarRecetas(
            Set<Integer> idsIngredientesDisponibles);
}
