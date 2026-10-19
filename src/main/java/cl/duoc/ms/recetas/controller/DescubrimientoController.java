
package cl.duoc.ms.recetas.controller;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;
import cl.duoc.ms.recetas.service.DescubrimientoService;

/**
 * Controlador REST para el descubrimiento de recetas.
 *
 * Recibe los ingredientes disponibles del usuario
 * y delega las recomendaciones al servicio de negocio.
 */
@RestController
@RequestMapping("/api/v1/descubrimiento")
public class DescubrimientoController {

    private final DescubrimientoService descubrimientoService;

    @Autowired
    public DescubrimientoController(
            DescubrimientoService descubrimientoService) {

        this.descubrimientoService = Objects.requireNonNull(
                descubrimientoService,
                "El servicio de descubrimiento es obligatorio.");
    }

    /**
     * Recomienda recetas según los ingredientes disponibles.
     *
     * Ejemplo:
     * GET /api/v1/descubrimiento/recomendaciones?ingredientes=1,2,3
     *
     * Respuestas:
     * 200 OK: lista de recomendaciones, posiblemente vacía.
     * 400 Bad Request: identificadores inválidos.
     *
     * Los ingredientes duplicados se eliminan mediante Set.
     */
    @GetMapping("/recomendaciones")
    public ResponseEntity<List<RecetaRecomendacionResponse>>
            recomendarRecetas(
                    @RequestParam("ingredientes")
                    Set<Integer> idsIngredientesDisponibles) {

        List<RecetaRecomendacionResponse> recomendaciones =
                descubrimientoService.recomendarRecetas(
                        idsIngredientesDisponibles);

        return ResponseEntity.ok(recomendaciones);
    }
}
