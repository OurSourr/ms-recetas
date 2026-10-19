
package cl.duoc.ms.recetas.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.ms.recetas.model.Ingrediente;
import cl.duoc.ms.recetas.repository.IngredienteRepository;
import cl.duoc.ms.recetas.service.IngredienteService;

/**
 * Implementación de la lógica de negocio para ingredientes.
 */
@Service
public class IngredienteServiceImpl implements IngredienteService {

    private final IngredienteRepository ingredienteRepository;

    @Autowired
    public IngredienteServiceImpl(IngredienteRepository ingredienteRepository) {
        this.ingredienteRepository = ingredienteRepository;
    }

    /**
     * Obtiene los ingredientes activos.
     *
     * @return lista de ingredientes activos
     */
    @Override
    public List<Ingrediente> listarIngredientesActivos() {
        return ingredienteRepository.findByActivoTrue();
    }
}
