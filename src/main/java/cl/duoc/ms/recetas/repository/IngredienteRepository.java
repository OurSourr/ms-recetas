package cl.duoc.ms.recetas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.Ingrediente;

/**
 * Repositorio de persistencia para los ingredientes.
 */
public interface IngredienteRepository
        extends JpaRepository<Ingrediente, Integer> {

    /**
     * Recupera únicamente los ingredientes activos.
     *
     * @return lista de ingredientes activos
     */
    List<Ingrediente> findByActivoTrue();
}