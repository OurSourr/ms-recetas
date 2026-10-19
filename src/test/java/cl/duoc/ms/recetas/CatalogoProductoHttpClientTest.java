
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
 * Pruebas unitarias del cliente HTTP de Catálogo.
 *
 * Las respuestas HTTP se simulan para evitar dependencias
 * del microservicio Catálogo durante las pruebas.
 */
class CatalogoProductoHttpClientTest {

    private static final String CATALOGO_BASE_URL =
            "http://localhost:8081";

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(3);

    private static final Duration READ_TIMEOUT =
            Duration.ofSeconds(5);

    private ClienteHttpSimulado clienteHttp;
    private CatalogoProductoHttpClient catalogoClient;

    @BeforeEach
    void configurar() {

        clienteHttp = new ClienteHttpSimulado();

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(clienteHttp);

        // Conserva la fábrica HTTP simulada.
        catalogoClient = new CatalogoProductoHttpClient(
                builder,
                CATALOGO_BASE_URL,
                CONNECT_TIMEOUT,
                READ_TIMEOUT,
                true);
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

    @Test
    void debeRechazarTimeoutDeConexionInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CatalogoProductoHttpClient(
                        RestClient.builder(),
                        CATALOGO_BASE_URL,
                        Duration.ZERO,
                        READ_TIMEOUT));
    }

    @Test
    void debeRechazarTimeoutDeRespuestaInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CatalogoProductoHttpClient(
                        RestClient.builder(),
                        CATALOGO_BASE_URL,
                        CONNECT_TIMEOUT,
                        Duration.ZERO));
    }

    /**
     * Fábrica HTTP simulada.
     *
     * Permite configurar el estado y contenido de las respuestas
     * sin realizar solicitudes de red.
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
                                    contenido.getBytes(
                                            StandardCharsets.UTF_8),
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
