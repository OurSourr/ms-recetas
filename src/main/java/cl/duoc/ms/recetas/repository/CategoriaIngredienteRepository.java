package cl.duoc.ms.recetas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.CategoriaIngrediente;

/**
 * Repositorio de persistencia para las categorías de ingredientes.
 */
public interface CategoriaIngredienteRepository
        extends JpaRepository<CategoriaIngrediente, Integer> {

    /**
     * Recupera únicamente las categorías activas.
     *
     * @return lista de categorías activas
     */
    List<CategoriaIngrediente> findByActivoTrue();
}