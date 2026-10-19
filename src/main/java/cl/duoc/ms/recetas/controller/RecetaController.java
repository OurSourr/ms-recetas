
package cl.duoc.ms.recetas.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms.recetas.dto.RecetaDetalleResponse;
import cl.duoc.ms.recetas.dto.RecetaMapper;
import cl.duoc.ms.recetas.dto.RecetaResponse;
import cl.duoc.ms.recetas.service.RecetaService;

/**
 * Controlador REST para las consultas públicas de recetas.
 *
 * Delega las reglas de negocio al servicio y devuelve
 * únicamente DTOs, evitando exponer entidades JPA.
 */
@RestController
@RequestMapping("/api/v1/recetas")
public class RecetaController {

    private final RecetaService recetaService;

    @Autowired
    public RecetaController(RecetaService recetaService) {

        this.recetaService = Objects.requireNonNull(
                recetaService,
                "El servicio de recetas es obligatorio.");
    }

    /**
     * Consulta las recetas publicadas y activas.
     *
     * El servicio filtra las recetas que incumplen las
     * reglas de visibilidad pública.
     *
     * GET /api/v1/recetas
     */
    @GetMapping
    public ResponseEntity<List<RecetaResponse>> listarRecetasPublicadas() {

        List<RecetaResponse> recetas = recetaService
                .listarRecetasPublicadas()
                .stream()
                .map(RecetaMapper::toResponse)
                .toList();

        return ResponseEntity.ok(recetas);
    }

    /**
     * Consulta el detalle público de una receta por su ID.
     *
     * El servicio verifica que la receta esté activa,
     * publicada, tenga ingredientes obligatorios y
     * al menos un producto activo en Catálogo.
     *
     * Los pasos de preparación se incluyen ordenados
     * por su número.
     *
     * GET /api/v1/recetas/{idReceta}
     *
     * Respuestas:
     * 200 OK: receta visible y detalle disponible.
     * 404 Not Found: receta inexistente o no visible.
     *
     * Los identificadores inválidos son rechazados por
     * el servicio mediante IllegalArgumentException;
     * su conversión a HTTP 400 requiere un manejador
     * de excepciones.
     */
    @GetMapping("/{idReceta}")
    public ResponseEntity<RecetaDetalleResponse> obtenerDetalleRecetaPublicada(
            @PathVariable("idReceta") Integer idReceta) {

        return recetaService
                .obtenerDetalleRecetaPublicada(idReceta)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
