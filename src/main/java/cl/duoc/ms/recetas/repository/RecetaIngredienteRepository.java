package cl.duoc.ms.recetas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaIngredienteId;

/**
 * Repositorio de persistencia para los ingredientes asociados
 * a cada receta.
 *
 * Utiliza una clave primaria compuesta.
 */
public interface RecetaIngredienteRepository
        extends JpaRepository<RecetaIngrediente, RecetaIngredienteId> {

    /**
     * Obtiene los ingredientes asociados a una receta,
     * respetando el orden definido para su presentación.
     *
     * @param idReceta identificador de la receta
     * @return ingredientes asociados, ordenados ascendentemente
     */
    List<RecetaIngrediente> findByReceta_IdRecetaOrderByOrdenAsc(
            Integer idReceta);
}