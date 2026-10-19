
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.duoc.ms.recetas.controller.RecetaController;
import cl.duoc.ms.recetas.dto.PasoRecetaResponse;
import cl.duoc.ms.recetas.dto.RecetaDetalleResponse;
import cl.duoc.ms.recetas.service.RecetaService;

/**
 * Pruebas unitarias del controlador de recetas.
 *
 * Verifican las respuestas HTTP construidas por el
 * controlador sin iniciar el servidor ni acceder
 * a MySQL o al microservicio de Catálogo.
 */
class RecetaControllerTest {

    private RecetaService recetaService;
    private RecetaController recetaController;

    @BeforeEach
    void configurar() {

        recetaService = mock(RecetaService.class);
        recetaController = new RecetaController(recetaService);
    }

    /**
     * Una receta pública válida debe responder con
     * HTTP 200 y conservar sus pasos de preparación.
     */
    @Test
    void debeRetornarDetallePublicoConEstado200() {

        RecetaDetalleResponse detalle = new RecetaDetalleResponse(
                1,
                "Gin Sour",
                "Cóctel con gin.",
                List.of(
                        new PasoRecetaResponse(
                                1, "Agregar hielo."),
                        new PasoRecetaResponse(
                                2, "Mezclar ingredientes.")));

        when(recetaService.obtenerDetalleRecetaPublicada(1))
                .thenReturn(Optional.of(detalle));

        ResponseEntity<RecetaDetalleResponse> respuesta =
                recetaController.obtenerDetalleRecetaPublicada(1);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(detalle, respuesta.getBody());
        assertEquals(2, respuesta.getBody().pasos().size());

        verify(recetaService).obtenerDetalleRecetaPublicada(1);
    }

    /**
     * Cuando el servicio no encuentra una receta
     * públicamente visible, el controlador responde 404.
     */
    @Test
    void debeRetornar404CuandoRecetaNoEsVisible() {

        when(recetaService.obtenerDetalleRecetaPublicada(99))
                .thenReturn(Optional.empty());

        ResponseEntity<RecetaDetalleResponse> respuesta =
                recetaController.obtenerDetalleRecetaPublicada(99);

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode());

        assertEquals(null, respuesta.getBody());

        verify(recetaService).obtenerDetalleRecetaPublicada(99);
    }
}
