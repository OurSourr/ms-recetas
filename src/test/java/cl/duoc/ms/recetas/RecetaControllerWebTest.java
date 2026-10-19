
package cl.duoc.ms.recetas;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.duoc.ms.recetas.controller.RecetaController;
import cl.duoc.ms.recetas.dto.PasoRecetaResponse;
import cl.duoc.ms.recetas.dto.RecetaDetalleResponse;
import cl.duoc.ms.recetas.dto.RecetaResponse;
import cl.duoc.ms.recetas.exception.GlobalExceptionHandler;
import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.service.RecetaService;

/**
 * Pruebas HTTP del controlador de recetas.
 *
 * Verifica rutas, códigos HTTP, respuestas JSON
 * y tratamiento centralizado de excepciones.
 *
 * No requiere iniciar el servidor, MySQL ni Catálogo.
 */
class RecetaControllerWebTest {

    private MockMvc mockMvc;
    private RecetaService recetaService;

    @BeforeEach
    void configurar() {

        recetaService = mock(RecetaService.class);

        RecetaController controlador =
                new RecetaController(recetaService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controlador)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Verifica que una receta pública válida responda
     * HTTP 200 con sus pasos correctamente serializados.
     */
    @Test
    void debeRetornarDetallePublicoEnJson() throws Exception {

        RecetaDetalleResponse detalle = new RecetaDetalleResponse(
                1,
                "Gin Sour",
                "Cóctel con gin.",
                List.of(
                        new PasoRecetaResponse(
                                1, "Agregar hielo."),
                        new PasoRecetaResponse(
                                2, "Mezclar ingredientes.")
                )
        );

        when(recetaService.obtenerDetalleRecetaPublicada(1))
                .thenReturn(Optional.of(detalle));

        mockMvc.perform(get("/api/v1/recetas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReceta").value(1))
                .andExpect(jsonPath("$.titulo").value("Gin Sour"))
                .andExpect(jsonPath("$.descripcion")
                        .value("Cóctel con gin."))
                .andExpect(jsonPath("$.pasos.length()").value(2))
                .andExpect(jsonPath("$.pasos[0].numeroPaso").value(1))
                .andExpect(jsonPath("$.pasos[0].instruccion")
                        .value("Agregar hielo."))
                .andExpect(jsonPath("$.pasos[1].numeroPaso").value(2))
                .andExpect(jsonPath("$.pasos[1].instruccion")
                        .value("Mezclar ingredientes."));

        verify(recetaService).obtenerDetalleRecetaPublicada(1);
        verifyNoMoreInteractions(recetaService);
    }

    /**
     * Una receta inexistente o no visible públicamente
     * debe responder HTTP 404.
     */
    @Test
    void debeRetornar404CuandoRecetaNoEsPublicamenteVisible()
            throws Exception {

        when(recetaService.obtenerDetalleRecetaPublicada(99))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/recetas/99"))
                .andExpect(status().isNotFound());

        verify(recetaService).obtenerDetalleRecetaPublicada(99);
        verifyNoMoreInteractions(recetaService);
    }

    /**
     * Un identificador inválido debe producir HTTP 400
     * mediante el manejador global de excepciones.
     */
    @Test
    void debeRetornar400CuandoIdentificadorEsInvalido()
            throws Exception {

        when(recetaService.obtenerDetalleRecetaPublicada(0))
                .thenThrow(new IllegalArgumentException(
                        "El identificador debe ser positivo."));

        mockMvc.perform(get("/api/v1/recetas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("La solicitud contiene parámetros inválidos."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(recetaService).obtenerDetalleRecetaPublicada(0);
        verifyNoMoreInteractions(recetaService);
    }

    /**
     * Verifica que el listado de recetas públicas
     * responda HTTP 200 con los datos en formato JSON.
     */
    @Test
    void debeRetornarListadoDeRecetasPublicadas()
            throws Exception {

        Receta receta = mock(Receta.class);

        when(receta.getIdReceta()).thenReturn(1);
        when(receta.getTitulo()).thenReturn("Gin Sour");
        when(receta.getDescripcion())
                .thenReturn("Cóctel con gin.");

        when(recetaService.listarRecetasPublicadas())
                .thenReturn(List.of(receta));

        mockMvc.perform(get("/api/v1/recetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idReceta").value(1))
                .andExpect(jsonPath("$[0].titulo")
                        .value("Gin Sour"))
                .andExpect(jsonPath("$[0].descripcion")
                        .value("Cóctel con gin."));

        verify(recetaService).listarRecetasPublicadas();
        verifyNoMoreInteractions(recetaService);
    }

    /**
     * Si no existen recetas públicas, el endpoint
     * debe devolver HTTP 200 y un arreglo JSON vacío.
     */
    @Test
    void debeRetornarListadoVacioCuandoNoExistenRecetasPublicadas()
            throws Exception {

        when(recetaService.listarRecetasPublicadas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/recetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(recetaService).listarRecetasPublicadas();
        verifyNoMoreInteractions(recetaService);
    }
}
