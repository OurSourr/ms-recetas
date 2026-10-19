
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;

import cl.duoc.ms.recetas.integration.CatalogoProductoHttpClient;

/**
 * Pruebas del cliente HTTP sin iniciar el microservicio Catálogo.
 */
class CatalogoProductoHttpClientTest {

    private ClienteHttpSimulado clienteHttp;
    private CatalogoProductoHttpClient catalogoClient;

    @BeforeEach
    void configurar() {
        clienteHttp = new ClienteHttpSimulado();

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(clienteHttp);

        catalogoClient = new CatalogoProductoHttpClient(
                builder,
                "http://localhost:8081");
    }

    @Test
    void debeReconocerProductoActivo() {
        clienteHttp.responder(
                HttpStatus.OK,
                """
                {"idProducto":10,"activo":true}
                """);

        assertTrue(catalogoClient.existeProductoActivo(10));
    }

    @Test
    void debeRechazarProductoInactivo() {
        clienteHttp.responder(
                HttpStatus.OK,
                """
                {"idProducto":10,"activo":false}
                """);

        assertFalse(catalogoClient.existeProductoActivo(10));
    }

    @Test
    void debeRechazarProductoInexistente() {
        clienteHttp.responder(
                HttpStatus.NOT_FOUND,
                """
                {"mensaje":"Producto no encontrado"}
                """);

        assertFalse(catalogoClient.existeProductoActivo(10));
    }

    @Test
    void debeRechazarRespuestaConIdentificadorDistinto() {
        clienteHttp.responder(
                HttpStatus.OK,
                """
                {"idProducto":99,"activo":true}
                """);

        assertThrows(
                IllegalStateException.class,
                () -> catalogoClient.existeProductoActivo(10));
    }

    @Test
    void debeRechazarRespuestaSinEstadoActivo() {
        clienteHttp.responder(
                HttpStatus.OK,
                """
                {"idProducto":10}
                """);

        assertThrows(
                IllegalStateException.class,
                () -> catalogoClient.existeProductoActivo(10));
    }

    @Test
    void debePropagarErrorDeCatalogo() {
        clienteHttp.responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                """
                {"mensaje":"Error interno"}
                """);

        assertThrows(
                IllegalStateException.class,
                () -> catalogoClient.existeProductoActivo(10));
    }

    @Test
    void debeRechazarIdentificadorInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> catalogoClient.existeProductoActivo(0));
    }

    /**
     * Simula respuestas HTTP de forma controlada.
     */
    private static class ClienteHttpSimulado
            implements ClientHttpRequestFactory {

        private HttpStatus estado;
        private String contenido;

        void responder(HttpStatus estado, String contenido) {
            this.estado = estado;
            this.contenido = contenido;
        }

        @Override
        public ClientHttpRequest createRequest(
                URI uri,
                HttpMethod metodo) throws IOException {

            if (estado == null) {
                throw new IOException(
                        "No se configuró una respuesta HTTP simulada.");
            }

            return new org.springframework.mock.http.client
                    .MockClientHttpRequest(metodo, uri) {

                @Override
                protected org.springframework.http.client.ClientHttpResponse
                        executeInternal() {

                    MockClientHttpResponse respuesta =
                            new MockClientHttpResponse(
                                    contenido.getBytes(StandardCharsets.UTF_8),
                                    estado);

                    respuesta.getHeaders().set(
                            "Content-Type",
                            "application/json");

                    return respuesta;
                }
            };
        }
    }
}
