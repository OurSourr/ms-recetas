
package cl.duoc.ms.recetas.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaIngredienteId;

/**
 * Repositorio de persistencia para los ingredientes
 * asociados a cada receta.
 *
 * Utiliza una clave primaria compuesta.
 */
public interface RecetaIngredienteRepository
        extends JpaRepository<RecetaIngrediente, RecetaIngredienteId> {

    /**
     * Obtiene los ingredientes asociados a una receta,
     * respetando el orden definido para su presentación.
     */
    List<RecetaIngrediente> findByReceta_IdRecetaOrderByOrdenAsc(
            Integer idReceta);

    /**
     * Recupera los ingredientes de varias recetas
     * mediante una única consulta a la base de datos.
     *
     * Permite validar RN-PROP-01 en listados públicos
     * sin realizar una consulta individual por receta.
     */
    List<RecetaIngrediente> findByReceta_IdRecetaIn(
            Collection<Integer> idsRecetas);
}
