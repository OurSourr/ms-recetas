package cl.duoc.ms.recetas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms.recetas.dto.IngredienteMapper;
import cl.duoc.ms.recetas.dto.IngredienteResponse;
import cl.duoc.ms.recetas.service.IngredienteService;

/**
 * Controlador REST para consultar los ingredientes
 * disponibles en el sistema.
 */
@RestController
@RequestMapping("/api/v1/ingredientes")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @Autowired
    public IngredienteController(IngredienteService ingredienteService) {
        this.ingredienteService = ingredienteService;
    }

    /**
     * Obtiene únicamente los ingredientes activos.
     *
     * @return HTTP 200 con la lista de ingredientes activos
     */
    @GetMapping
    public ResponseEntity<List<IngredienteResponse>> listarIngredientesActivos() {

        List<IngredienteResponse> ingredientes = ingredienteService
                .listarIngredientesActivos()
                .stream()
                .map(IngredienteMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ingredientes);
    }
}