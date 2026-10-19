package cl.duoc.ms.recetas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.ms.recetas.model.Ingrediente;

/**
 * Repositorio de persistencia para los ingredientes.
 * Proporciona operaciones CRUD mediante Spring Data JPA.
 */
public interface IngredienteRepository
        extends JpaRepository<Ingrediente, Integer> {

}