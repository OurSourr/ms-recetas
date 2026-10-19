
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaIngredienteId;
import cl.duoc.ms.recetas.model.RecetaProducto;
import cl.duoc.ms.recetas.model.RecetaProductoId;
import cl.duoc.ms.recetas.repository.RecetaIngredienteRepository;
import cl.duoc.ms.recetas.repository.RecetaProductoRepository;
import cl.duoc.ms.recetas.repository.RecetaRepository;
import cl.duoc.ms.recetas.service.CatalogoProductoService;
import cl.duoc.ms.recetas.service.RecetaValidator;
import cl.duoc.ms.recetas.service.impl.RecetaServiceImpl;

/**
 * Pruebas unitarias de las reglas de negocio de recetas.
 *
 * Se utilizan repositorios y Catálogo simulados,
 * manteniendo el validador de negocio real.
 */
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

        recetaValidator = new RecetaValidator();

        recetaService = new RecetaServiceImpl(
                recetaRepository,
                recetaIngredienteRepository,
                recetaProductoRepository,
                recetaValidator,
                catalogoProductoService);
    }

    // =========================================================
    // PRUEBAS DE PUBLICACIÓN
    // =========================================================

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

    // =========================================================
    // PRUEBAS DE CONSULTA PÚBLICA
    // =========================================================

    @Test
    void debeRetornarListaVaciaSinRecetasPublicadas() {

        when(recetaRepository.findByPublicadaTrueAndActivoTrue())
                .thenReturn(List.of());

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(
                recetaIngredienteRepository,
                recetaProductoRepository,
                catalogoProductoService);
    }

    @Test
    void debeListarRecetaConIngredienteYProductoActivo() {

        Receta receta = prepararRecetaParaConsulta(1, true, 10);

        when(catalogoProductoService.existeProductoActivo(10))
                .thenReturn(true);

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertEquals(List.of(receta), resultado);

        verify(recetaIngredienteRepository)
                .findByReceta_IdRecetaIn(List.of(1));

        verify(recetaProductoRepository)
                .findByReceta_IdRecetaIn(List.of(1));

        verify(catalogoProductoService)
                .existeProductoActivo(10);
    }

    @Test
    void debeExcluirRecetaSinIngredientesObligatorios() {

        prepararRecetaParaConsulta(2, false, 10);

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(catalogoProductoService);
        verify(recetaRepository, never()).save(any());
    }

    @Test
    void debeExcluirRecetaSinIngredientes() {

        Receta receta = crearReceta(3);

        when(recetaRepository.findByPublicadaTrueAndActivoTrue())
                .thenReturn(List.of(receta));

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaIn(List.of(3)))
                .thenReturn(List.of());

        when(recetaProductoRepository
                .findByReceta_IdRecetaIn(List.of(3)))
                .thenReturn(List.of());

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(catalogoProductoService);
    }

    @Test
    void debeExcluirRecetaSinProductosAsociados() {

        Receta receta = crearReceta(4);
        RecetaIngrediente ingrediente =
                crearIngrediente(4, 1, true);

        when(recetaRepository.findByPublicadaTrueAndActivoTrue())
                .thenReturn(List.of(receta));

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaIn(List.of(4)))
                .thenReturn(List.of(ingrediente));

        when(recetaProductoRepository
                .findByReceta_IdRecetaIn(List.of(4)))
                .thenReturn(List.of());

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(catalogoProductoService);
    }

    @Test
    void debeExcluirRecetaConProductoInactivo() {

        prepararRecetaParaConsulta(5, true, 10);

        when(catalogoProductoService.existeProductoActivo(10))
                .thenReturn(false);

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertTrue(resultado.isEmpty());

        verify(catalogoProductoService)
                .existeProductoActivo(10);
    }

    @Test
    void debeExcluirRecetaSiCatalogoNoResponde() {

        prepararRecetaParaConsulta(6, true, 10);

        when(catalogoProductoService.existeProductoActivo(10))
                .thenThrow(new IllegalStateException(
                        "Catálogo no disponible."));

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertTrue(resultado.isEmpty());

        verify(catalogoProductoService)
                .existeProductoActivo(10);
    }

    @Test
    void debeConservarSoloRecetasValidasEnConsultaMixta() {

        Receta valida = crearReceta(1);
        Receta sinObligatorio = crearReceta(2);
        Receta productoInactivo = crearReceta(3);

        RecetaIngrediente obligatorio =
                crearIngrediente(1, 101, true);

        RecetaIngrediente opcional =
                crearIngrediente(2, 102, false);

        RecetaIngrediente otroObligatorio =
                crearIngrediente(3, 103, true);

        RecetaProducto productoValido =
                crearProducto(1, 10);

        RecetaProducto otroProducto =
                crearProducto(3, 20);

        List<Integer> ids = List.of(1, 2, 3);

        when(recetaRepository.findByPublicadaTrueAndActivoTrue())
                .thenReturn(List.of(
                        valida,
                        sinObligatorio,
                        productoInactivo));

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaIn(ids))
                .thenReturn(List.of(
                        obligatorio,
                        opcional,
                        otroObligatorio));

        when(recetaProductoRepository
                .findByReceta_IdRecetaIn(ids))
                .thenReturn(List.of(
                        productoValido,
                        otroProducto));

        when(catalogoProductoService.existeProductoActivo(10))
                .thenReturn(true);

        when(catalogoProductoService.existeProductoActivo(20))
                .thenReturn(false);

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertEquals(List.of(valida), resultado);

        verify(recetaIngredienteRepository)
                .findByReceta_IdRecetaIn(ids);

        verify(recetaProductoRepository)
                .findByReceta_IdRecetaIn(ids);

        verify(recetaRepository, never()).save(any());
    }

    @Test
    void debeConsultarProductoCompartidoUnaSolaVez() {

        Receta primera = crearReceta(1);
        Receta segunda = crearReceta(2);

        List<Integer> ids = List.of(1, 2);

        // Los mocks se crean antes de configurar los repositorios.
        RecetaIngrediente ingredientePrimera =
                crearIngrediente(1, 101, true);

        RecetaIngrediente ingredienteSegunda =
                crearIngrediente(2, 102, true);

        RecetaProducto productoPrimera =
                crearProducto(1, 10);

        RecetaProducto productoSegunda =
                crearProducto(2, 10);

        when(recetaRepository.findByPublicadaTrueAndActivoTrue())
                .thenReturn(List.of(primera, segunda));

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaIn(ids))
                .thenReturn(List.of(
                        ingredientePrimera,
                        ingredienteSegunda));

        when(recetaProductoRepository
                .findByReceta_IdRecetaIn(ids))
                .thenReturn(List.of(
                        productoPrimera,
                        productoSegunda));

        when(catalogoProductoService.existeProductoActivo(10))
                .thenReturn(true);

        List<Receta> resultado =
                recetaService.listarRecetasPublicadas();

        assertEquals(List.of(primera, segunda), resultado);

        verify(catalogoProductoService, times(1))
                .existeProductoActivo(10);
    }

    // =========================================================
    // MÉTODOS REUTILIZABLES PARA PREPARAR DATOS
    // =========================================================

    /**
     * Crea una receta simulada con identificador conocido.
     */
    private Receta crearReceta(Integer idReceta) {

        Receta receta = mock(Receta.class);

        when(receta.getIdReceta()).thenReturn(idReceta);

        return receta;
    }

    /**
     * Crea un ingrediente asociado a una receta.
     */
    private RecetaIngrediente crearIngrediente(
            Integer idReceta,
            Integer idIngrediente,
            boolean obligatorio) {

        RecetaIngrediente ingrediente =
                mock(RecetaIngrediente.class);

        when(ingrediente.getId())
                .thenReturn(new RecetaIngredienteId(
                        idReceta, idIngrediente));

        when(ingrediente.isObligatorio())
                .thenReturn(obligatorio);

        return ingrediente;
    }

    /**
     * Crea una asociación entre receta y producto externo.
     */
    private RecetaProducto crearProducto(
            Integer idReceta,
            Integer idProducto) {

        RecetaProducto producto = mock(RecetaProducto.class);

        when(producto.getId())
                .thenReturn(new RecetaProductoId(
                        idReceta, idProducto));

        return producto;
    }

    /**
     * Prepara una receta para pruebas de consulta pública.
     *
     * Evita crear mocks dentro de thenReturn(), ya que
     * eso provoca UnfinishedStubbingException en Mockito.
     */
    private Receta prepararRecetaParaConsulta(
            Integer idReceta,
            boolean obligatorio,
            Integer idProducto) {

        Receta receta = crearReceta(idReceta);

        RecetaIngrediente ingrediente =
                crearIngrediente(idReceta, 100, obligatorio);

        RecetaProducto producto =
                crearProducto(idReceta, idProducto);

        when(recetaRepository.findByPublicadaTrueAndActivoTrue())
                .thenReturn(List.of(receta));

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaIn(List.of(idReceta)))
                .thenReturn(List.of(ingrediente));

        when(recetaProductoRepository
                .findByReceta_IdRecetaIn(List.of(idReceta)))
                .thenReturn(List.of(producto));

        return receta;
    }

    /**
     * Prepara datos comunes para las pruebas de publicación.
     */
    private void prepararRecetaValidaConProducto(
            Integer idReceta,
            Integer idProducto) {

        Receta receta = mock(Receta.class);
        RecetaIngrediente ingrediente =
                mock(RecetaIngrediente.class);
        RecetaProducto producto =
                mock(RecetaProducto.class);

        when(recetaRepository.findById(idReceta))
                .thenReturn(Optional.of(receta));

        when(receta.isActivo()).thenReturn(true);
        when(ingrediente.isObligatorio()).thenReturn(true);

        when(recetaIngredienteRepository
                .findByReceta_IdRecetaOrderByOrdenAsc(idReceta))
                .thenReturn(List.of(ingrediente));

        when(producto.getId())
                .thenReturn(new RecetaProductoId(
                        idReceta, idProducto));

        when(recetaProductoRepository
                .findByReceta_IdReceta(idReceta))
                .thenReturn(List.of(producto));

        when(recetaRepository.save(receta))
                .thenReturn(receta);
    }
}
