package cl.duoc.ms.recetas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms.recetas.dto.RecetaMapper;
import cl.duoc.ms.recetas.dto.RecetaResponse;
import cl.duoc.ms.recetas.service.RecetaService;

@RestController
@RequestMapping("/api/v1/recetas")
public class RecetaController {

    private final RecetaService recetaService;

    @Autowired
    public RecetaController(RecetaService recetaService) {
        this.recetaService = recetaService;
    }

    /**
     * Consulta las recetas publicadas y activas.
     * Convierte las entidades a DTOs antes de responder.
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
}