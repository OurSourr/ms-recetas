package cl.duoc.ms.recetas.repository;

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

}