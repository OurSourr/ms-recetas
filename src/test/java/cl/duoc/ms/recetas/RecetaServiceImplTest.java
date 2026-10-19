
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaProducto;
import cl.duoc.ms.recetas.model.RecetaProductoId;
import cl.duoc.ms.recetas.repository.RecetaIngredienteRepository;
import cl.duoc.ms.recetas.repository.RecetaProductoRepository;
import cl.duoc.ms.recetas.repository.RecetaRepository;
import cl.duoc.ms.recetas.service.CatalogoProductoService;
import cl.duoc.ms.recetas.service.RecetaValidator;
import cl.duoc.ms.recetas.service.impl.RecetaServiceImpl;

class RecetaServiceImplTest {

    private RecetaRepository recetaRepository;
    private RecetaIngredienteRepository recetaIngredienteRepository;
    private RecetaProductoRepository recetaProductoRepository;
    private CatalogoProductoService catalogoProductoService;
    private RecetaValidator recetaValidator;
    private RecetaServiceImpl recetaService;

    @BeforeEach
    void configurar() {
        recetaRepository = mock(RecetaRepository.class);
        recetaIngredienteRepository = mock(RecetaIngredienteRepository.class);
        recetaProductoRepository = mock(RecetaProductoRepository.class);
        catalogoProductoService = mock(CatalogoProductoService.class);

        // Se utiliza el validador real para comprobar las reglas de negocio.
        recetaValidator = new RecetaValidator();

        recetaService = new RecetaServiceImpl(
                recetaRepository,
                recetaIngredienteRepository,
                recetaProductoRepository,
                recetaValidator,
                catalogoProductoService);
    }

    @Test
    void debeRechazarIdentificadorInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> recetaService.publicarReceta(0));

        verifyNoInteractions(
                recetaRepository,
                recetaIngredienteRepository,
                recetaProductoRepository,
                catalogoProductoService);
    }

    @Test
    void debeRechazarRecetaInexistente() {
        when(recetaRepository.findById(10))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> recetaService.publicarReceta(10));

        verify(recetaRepository, never()).save(any());
        verifyNoInteractions(
                recetaIngredienteRepository,
                recetaProductoRepository,
                catalogoProductoService);
    }

    @Test
    void debeRechazarRecetaInactiva() {
        Receta receta = mock(Receta.class);

        when(recetaRepository.findById(1))
                .thenReturn(Optional.of(receta));
        when(receta.isActivo()).thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> recetaService.publicarReceta(1));

        verify(recetaRepository, never()).save(any());
        verifyNoInteractions(
                recetaIngredienteRepository,
                recetaProductoRepository,
                catalogoProductoService);
    }

    @Test
    void debeRechazarRecetaSinIngredientesObligatorios() {
        Receta receta = mock(Receta.class);
        RecetaIngrediente ingrediente = mock(RecetaIngrediente.class);

        when(recetaRepository.findById(1))
                .thenReturn(Optional.of(receta));
        when(receta.isActivo()).thenReturn(true);
        when(ingrediente.isObligatorio()).thenReturn(false);

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaOrderByOrdenAsc(1))
                .thenReturn(List.of(ingrediente));

        assertThrows(
                IllegalStateException.class,
                () -> recetaService.publicarReceta(1));

        verify(receta, never()).marcarComoPublicada();
        verify(recetaRepository, never()).save(any());
        verifyNoInteractions(
                recetaProductoRepository,
                catalogoProductoService);
    }

    @Test
    void debeRechazarRecetaSinProductosAsociados() {
        Receta receta = mock(Receta.class);
        RecetaIngrediente ingrediente = mock(RecetaIngrediente.class);

        when(recetaRepository.findById(1))
                .thenReturn(Optional.of(receta));
        when(receta.isActivo()).thenReturn(true);
        when(ingrediente.isObligatorio()).thenReturn(true);

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaOrderByOrdenAsc(1))
                .thenReturn(List.of(ingrediente));

        when(recetaProductoRepository.findByReceta_IdReceta(1))
                .thenReturn(List.of());

        assertThrows(
                IllegalStateException.class,
                () -> recetaService.publicarReceta(1));

        verify(receta, never()).marcarComoPublicada();
        verify(recetaRepository, never()).save(any());
        verifyNoInteractions(catalogoProductoService);
    }

    @Test
    void debePublicarRecetaConProductoActivo() {
        prepararRecetaValidaConProducto(1, 10);

        when(catalogoProductoService.existeProductoActivo(10))
                .thenReturn(true);

        Receta receta = recetaRepository.findById(1).orElseThrow();

        Receta resultado = recetaService.publicarReceta(1);

        assertEquals(receta, resultado);
        verify(catalogoProductoService).existeProductoActivo(10);
        verify(receta).marcarComoPublicada();
        verify(recetaRepository).save(receta);
    }

    @Test
    void debeRechazarRecetaConProductoInactivo() {
        prepararRecetaValidaConProducto(1, 10);

        when(catalogoProductoService.existeProductoActivo(10))
                .thenReturn(false);

        Receta receta = recetaRepository.findById(1).orElseThrow();

        assertThrows(
                IllegalStateException.class,
                () -> recetaService.publicarReceta(1));

        verify(catalogoProductoService).existeProductoActivo(10);
        verify(receta, never()).marcarComoPublicada();
        verify(recetaRepository, never()).save(any());
    }

    @Test
    void debeImpedirPublicacionSiCatalogoNoResponde() {
        prepararRecetaValidaConProducto(1, 10);

        when(catalogoProductoService.existeProductoActivo(10))
                .thenThrow(new IllegalStateException(
                        "No fue posible verificar el producto en Catálogo."));

        Receta receta = recetaRepository.findById(1).orElseThrow();

        assertThrows(
                IllegalStateException.class,
                () -> recetaService.publicarReceta(1));

        verify(receta, never()).marcarComoPublicada();
        verify(recetaRepository, never()).save(any());
    }

    /**
     * Prepara los datos comunes de las pruebas de publicación.
     * Evita duplicar la configuración de repositorios y entidades.
     */
    private void prepararRecetaValidaConProducto(
            Integer idReceta,
            Integer idProducto) {

        Receta receta = mock(Receta.class);
        RecetaIngrediente ingrediente = mock(RecetaIngrediente.class);
        RecetaProducto producto = mock(RecetaProducto.class);

        when(recetaRepository.findById(idReceta))
                .thenReturn(Optional.of(receta));
        when(receta.isActivo()).thenReturn(true);
        when(ingrediente.isObligatorio()).thenReturn(true);

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaOrderByOrdenAsc(idReceta))
                .thenReturn(List.of(ingrediente));

        when(producto.getId())
                .thenReturn(new RecetaProductoId(idReceta, idProducto));

        when(recetaProductoRepository.findByReceta_IdReceta(idReceta))
                .thenReturn(List.of(producto));

        when(recetaRepository.save(receta))
                .thenReturn(receta);
    }
}
