package cl.duoc.ms.recetas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.ms.recetas.model.PasoReceta;

/**
 * Repositorio de persistencia para los pasos de preparación
 * asociados a las recetas.
 *
 * Proporciona operaciones CRUD mediante Spring Data JPA.
 */
public interface PasoRecetaRepository
        extends JpaRepository<PasoReceta, Integer> {

}