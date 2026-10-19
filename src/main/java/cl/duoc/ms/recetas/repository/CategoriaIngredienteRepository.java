package cl.duoc.ms.recetas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.ms.recetas.model.CategoriaIngrediente;

/**
 * Repositorio de persistencia para las categorías de ingredientes.
 * Proporciona operaciones CRUD mediante Spring Data JPA.
 */
public interface CategoriaIngredienteRepository
        extends JpaRepository<CategoriaIngrediente, Integer> {

}