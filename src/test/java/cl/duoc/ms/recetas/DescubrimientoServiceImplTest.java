
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;
import cl.duoc.ms.recetas.model.Ingrediente;
import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaIngredienteId;
import cl.duoc.ms.recetas.repository.RecetaIngredienteRepository;
import cl.duoc.ms.recetas.service.CalculadorCompatibilidad;
import cl.duoc.ms.recetas.service.RecetaService;
import cl.duoc.ms.recetas.service.impl.DescubrimientoServiceImpl;

/**
 * Pruebas unitarias del servicio de descubrimiento.
 *
 * Se simulan las consultas a datos y se utiliza
 * el calculador real de compatibilidad.
 */
class DescubrimientoServiceImplTest {

    private RecetaService recetaService;
    private RecetaIngredienteRepository ingredienteRepository;
    private DescubrimientoServiceImpl descubrimientoService;

    @BeforeEach
    void configurar() {

        recetaService = mock(RecetaService.class);
        ingredienteRepository =
                mock(RecetaIngredienteRepository.class);

        descubrimientoService = new DescubrimientoServiceImpl(
                recetaService,
                ingredienteRepository,
                new CalculadorCompatibilidad());
    }

    @Test
    void debeRetornarListaVaciaSiNoHayRecetasPublicas() {

        when(recetaService.listarRecetasPublicadas())
                .thenReturn(List.of());

        List<RecetaRecomendacionResponse> resultado =
                descubrimientoService.recomendarRecetas(Set.of(10));

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(ingredienteRepository);
    }

    @Test
    void debeRecomendarRecetaConCompatibilidadCompleta() {

        Receta receta = crearReceta(1, "Gin Sour");

        RecetaIngrediente gin =
                crearIngrediente(1, 10, "Gin");

        when(recetaService.listarRecetasPublicadas())
                .thenReturn(List.of(receta));

        when(ingredienteRepository.findByReceta_IdRecetaIn(
                List.of(1)))
                .thenReturn(List.of(gin));

        List<RecetaRecomendacionResponse> resultado =
                descubrimientoService.recomendarRecetas(Set.of(10));

        assertEquals(1, resultado.size());
        assertEquals(100.0,
                resultado.get(0).porcentajeCompatibilidad());
        assertTrue(resultado.get(0).puedePreparar());
    }

    @Test
    void debeOrdenarPorMayorCompatibilidad() {

        Receta recetaUno =
                crearReceta(1, "Receta parcial");

        Receta recetaDos =
                crearReceta(2, "Receta completa");

        RecetaIngrediente ingredienteUno =
                crearIngrediente(1, 10, "Gin");

        RecetaIngrediente ingredienteDos =
                crearIngrediente(1, 20, "Limón");

        RecetaIngrediente ingredienteTres =
                crearIngrediente(2, 10, "Gin");

        when(recetaService.listarRecetasPublicadas())
                .thenReturn(List.of(recetaUno, recetaDos));

        when(ingredienteRepository.findByReceta_IdRecetaIn(
                List.of(1, 2)))
                .thenReturn(List.of(
                        ingredienteUno,
                        ingredienteDos,
                        ingredienteTres));

        List<RecetaRecomendacionResponse> resultado =
                descubrimientoService.recomendarRecetas(Set.of(10));

        assertEquals(2, resultado.size());

        assertEquals(2, resultado.get(0).idReceta());
        assertEquals(100.0,
                resultado.get(0).porcentajeCompatibilidad());

        assertEquals(1, resultado.get(1).idReceta());
        assertEquals(50.0,
                resultado.get(1).porcentajeCompatibilidad());
    }

    @Test
    void debeConsultarIngredientesEnUnaSolaOperacion() {

        Receta recetaUno = crearReceta(1, "Receta uno");
        Receta recetaDos = crearReceta(2, "Receta dos");

        RecetaIngrediente ingredienteUno =
                crearIngrediente(1, 10, "Gin");

        RecetaIngrediente ingredienteDos =
                crearIngrediente(2, 20, "Limón");

        when(recetaService.listarRecetasPublicadas())
                .thenReturn(List.of(recetaUno, recetaDos));

        when(ingredienteRepository.findByReceta_IdRecetaIn(
                List.of(1, 2)))
                .thenReturn(List.of(
                        ingredienteUno,
                        ingredienteDos));

        descubrimientoService.recomendarRecetas(Set.of(10));

        verify(ingredienteRepository, times(1))
                .findByReceta_IdRecetaIn(List.of(1, 2));

        verifyNoMoreInteractions(ingredienteRepository);
    }

    @Test
    void debeRechazarIdentificadoresInvalidosSinConsultarDatos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> descubrimientoService.recomendarRecetas(
                        Set.of(0)));

        verifyNoInteractions(
                recetaService,
                ingredienteRepository);
    }

    @Test
    void debeRechazarConjuntoNuloSinConsultarDatos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> descubrimientoService.recomendarRecetas(null));

        verifyNoInteractions(
                recetaService,
                ingredienteRepository);
    }

    /**
     * Crea una receta simulada para las pruebas.
     */
    private Receta crearReceta(Integer id, String titulo) {

        Receta receta = mock(Receta.class);

        when(receta.getIdReceta()).thenReturn(id);
        when(receta.getTitulo()).thenReturn(titulo);

        return receta;
    }

    /**
     * Crea un ingrediente obligatorio y activo
     * asociado a una receta.
     */
    private RecetaIngrediente crearIngrediente(
            Integer idReceta,
            Integer idIngrediente,
            String nombre) {

        Ingrediente ingrediente = mock(Ingrediente.class);
        RecetaIngrediente relacion =
                mock(RecetaIngrediente.class);

        when(ingrediente.getIdIngrediente())
                .thenReturn(idIngrediente);

        when(ingrediente.getNombre())
                .thenReturn(nombre);

        when(ingrediente.isActivo())
                .thenReturn(true);

        when(relacion.isObligatorio())
                .thenReturn(true);

        when(relacion.getIngrediente())
                .thenReturn(ingrediente);

        when(relacion.getId())
                .thenReturn(new RecetaIngredienteId(
                        idReceta,
                        idIngrediente));

        return relacion;
    }
}
