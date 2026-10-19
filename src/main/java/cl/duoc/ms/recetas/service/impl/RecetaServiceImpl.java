
package cl.duoc.ms.recetas.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaProducto;
import cl.duoc.ms.recetas.repository.RecetaIngredienteRepository;
import cl.duoc.ms.recetas.repository.RecetaProductoRepository;
import cl.duoc.ms.recetas.repository.RecetaRepository;
import cl.duoc.ms.recetas.service.CatalogoProductoService;
import cl.duoc.ms.recetas.service.RecetaService;
import cl.duoc.ms.recetas.service.RecetaValidator;

/**
 * Implementación de las operaciones de negocio para recetas.
 */
@Service
public class RecetaServiceImpl implements RecetaService {

    private final RecetaRepository recetaRepository;
    private final RecetaIngredienteRepository recetaIngredienteRepository;
    private final RecetaProductoRepository recetaProductoRepository;
    private final RecetaValidator recetaValidator;
    private final CatalogoProductoService catalogoProductoService;

    @Autowired
    public RecetaServiceImpl(
            RecetaRepository recetaRepository,
            RecetaIngredienteRepository recetaIngredienteRepository,
            RecetaProductoRepository recetaProductoRepository,
            RecetaValidator recetaValidator,
            CatalogoProductoService catalogoProductoService) {

        this.recetaRepository = recetaRepository;
        this.recetaIngredienteRepository = recetaIngredienteRepository;
        this.recetaProductoRepository = recetaProductoRepository;
        this.recetaValidator = recetaValidator;
        this.catalogoProductoService = catalogoProductoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receta> listarRecetasPublicadas() {
        return recetaRepository.findByPublicadaTrueAndActivoTrue();
    }

    /**
     * Publica una receta únicamente cuando cumple
     * las reglas de negocio correspondientes.
     */
    @Override
    @Transactional
    public Receta publicarReceta(Integer idReceta) {

        if (idReceta == null || idReceta <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la receta debe ser positivo.");
        }

        Receta receta = recetaRepository.findById(idReceta)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una receta con identificador: " + idReceta));

        if (!receta.isActivo()) {
            throw new IllegalStateException(
                    "No se puede publicar una receta inactiva.");
        }

        List<RecetaIngrediente> ingredientes =
                recetaIngredienteRepository
                        .findByReceta_IdRecetaOrderByOrdenAsc(idReceta);

        // RN-PROP-01: exige al menos un ingrediente obligatorio.
        recetaValidator.validarIngredientesObligatorios(ingredientes);

        List<RecetaProducto> productos =
                recetaProductoRepository.findByReceta_IdReceta(idReceta);

        // Exige al menos una asociación válida con Catálogo.
        recetaValidator.validarProductosAsociados(productos);

        // Catálogo es responsable de determinar si un producto está activo.
        // Si Catálogo falla, la excepción impide publicar la receta.
        boolean existeProductoActivo = productos.stream()
                .filter(producto -> producto != null
                        && producto.getId() != null
                        && producto.getId().getIdProductoExterno() != null
                        && producto.getId().getIdProductoExterno() > 0)
                .anyMatch(producto -> catalogoProductoService.existeProductoActivo(
                        producto.getId().getIdProductoExterno()));

        if (!existeProductoActivo) {
            throw new IllegalStateException(
                    "La receta debe tener al menos un producto activo en Catálogo.");
        }

        receta.marcarComoPublicada();

        return recetaRepository.save(receta);
    }
}
