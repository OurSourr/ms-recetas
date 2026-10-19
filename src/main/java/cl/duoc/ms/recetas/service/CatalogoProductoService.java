package cl.duoc.ms.recetas.service;

/**
 * Contrato para consultar información de productos
 * administrados por el microservicio de Catálogo.
 *
 * Evita acoplar la lógica de recetas a la implementación
 * concreta de la comunicación entre microservicios.
 */
public interface CatalogoProductoService {

    /**
     * Comprueba si un producto externo existe y está activo
     * en el microservicio de Catálogo.
     *
     * @param idProductoExterno identificador del producto
     * @return true si el producto existe y está activo;
     *         false si no existe o está inactivo
     */
    boolean existeProductoActivo(Integer idProductoExterno);
}