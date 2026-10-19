package cl.duoc.ms.recetas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.ms.recetas.model.Receta;

/**
 * Repositorio de persistencia para la entidad Receta.
 * Proporciona operaciones CRUD mediante Spring Data JPA.
 */
public interface RecetaRepository extends JpaRepository<Receta, Integer> {

}