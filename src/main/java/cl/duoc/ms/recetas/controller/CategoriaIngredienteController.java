
package cl.duoc.ms.recetas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms.recetas.dto.CategoriaIngredienteMapper;
import cl.duoc.ms.recetas.dto.CategoriaIngredienteResponse;
import cl.duoc.ms.recetas.service.CategoriaIngredienteService;

/**
 * Controlador REST para consultar las categorías
 * de ingredientes disponibles.
 */
@RestController
@RequestMapping("/api/v1/categorias-ingredientes")
public class CategoriaIngredienteController {

    private final CategoriaIngredienteService categoriaIngredienteService;

    @Autowired
    public CategoriaIngredienteController(
            CategoriaIngredienteService categoriaIngredienteService) {
        this.categoriaIngredienteService = categoriaIngredienteService;
    }

    /**
     * Obtiene únicamente las categorías activas.
     *
     * @return HTTP 200 con la lista de categorías;
     *         una lista vacía si no existen categorías activas
     */
    @GetMapping
    public ResponseEntity<List<CategoriaIngredienteResponse>> listarCategorias() {

        List<CategoriaIngredienteResponse> categorias =
                categoriaIngredienteService.listarCategoriasActivas()
                        .stream()
                        .map(CategoriaIngredienteMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(categorias);
    }
}
