
package cl.duoc.ms.recetas.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.ms.recetas.model.CategoriaIngrediente;
import cl.duoc.ms.recetas.repository.CategoriaIngredienteRepository;
import cl.duoc.ms.recetas.service.CategoriaIngredienteService;

/**
 * Implementación de la lógica de negocio para las categorías
 * de ingredientes.
 */
@Service
public class CategoriaIngredienteServiceImpl
        implements CategoriaIngredienteService {

    private final CategoriaIngredienteRepository categoriaIngredienteRepository;

    @Autowired
    public CategoriaIngredienteServiceImpl(
            CategoriaIngredienteRepository categoriaIngredienteRepository) {
        this.categoriaIngredienteRepository = categoriaIngredienteRepository;
    }

    /**
     * Obtiene las categorías activas.
     *
     * @return lista de categorías activas
     */
    @Override
    public List<CategoriaIngrediente> listarCategoriasActivas() {
        return categoriaIngredienteRepository.findByActivoTrue();
    }
}
