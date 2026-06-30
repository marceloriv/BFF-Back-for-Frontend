package com.ticketti.bff.integration;

import com.ticketti.bff.client.UsuarioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas de integración para validar rutas protegidas del BFF.
// Se prueban rutas públicas, rutas autenticadas y rutas protegidas por rol.
@SpringBootTest(properties = {
        // Se evita depender del config server durante las pruebas.
        "spring.config.import=optional:",
        "spring.cloud.config.enabled=false",

        // URL de prueba para levantar el contexto del BFF.
        "api.gateway.url=http://localhost:8222",

        // Secreto solo para pruebas JWT.
        "jwt.secret=ticketti-jwt-secret-key-2024-secure-random-256-bit-test",

        // Variables de RabbitMQ de prueba.
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityRoutesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Se mockea para no depender de ms-usuarios ni del API Gateway real.
    @MockitoBean
    private UsuarioClient usuarioClient;

    // Valida que una ruta pública o permitida por rol no sea bloqueada por seguridad.
    // Puede responder 400, 404 o 405 si no existe controlador final, pero no 401 ni 403.
    private void noDebeSerBloqueoDeSeguridad(ResultActions resultado) throws Exception {
        resultado.andExpect(result -> {
            int estado = result.getResponse().getStatus();

            if (estado == 401 || estado == 403) {
                throw new AssertionError("La ruta fue bloqueada por seguridad con estado: " + estado);
            }
        });
    }

    // Valida que una ruta protegida sin autenticación sea bloqueada por seguridad.
    private void debeSerBloqueoDeSeguridad(ResultActions resultado) throws Exception {
        resultado.andExpect(result -> {
            int estado = result.getResponse().getStatus();

            if (estado != 401 && estado != 403) {
                throw new AssertionError("Se esperaba 401 o 403, pero retornó: " + estado);
            }
        });
    }

    // ══════════════════════════════════════════════════════
    // USUARIOS

    @Test
    void crearUsuario_sinAutenticacion_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // Registro público: POST /api/v1/usuarios.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nombre": "Usuario Prueba",
                                    "correo": "test@ticketti.cl",
                                    "contrasena": "Password123",
                                    "rol": "CLIENTE",
                                    "telefono": "912345678",
                                    "direccion": "Direccion prueba",
                                    "aceptaTerminos": true,
                                    "aceptaPrivacidad": true
                                }
                                """))
        );
    }

    @Test
    void listarUsuarios_sinAutenticacion_deberiaSerBloqueado() throws Exception {
        // GET /api/v1/usuarios requiere ADMIN o ADMINPLATAFORMA.
        debeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarUsuarios_conRolCliente_deberiaRetornarForbidden() throws Exception {
        // CLIENTE no puede listar todos los usuarios.
        mockMvc.perform(get("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINPLATAFORMA")
    void cambiarRol_conRolAdminPlataforma_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // ADMINPLATAFORMA sí puede pasar la seguridad para cambiar roles.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(patch("/api/v1/usuarios/1/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rol": "CLIENTE"
                                }
                                """))
        );
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void cambiarRol_conRolOrganizador_deberiaRetornarForbidden() throws Exception {
        // ORGANIZADOR no puede cambiar roles.
        mockMvc.perform(patch("/api/v1/usuarios/1/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rol": "ADMINPLATAFORMA"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    // ══════════════════════════════════════════════════════
    // EVENTOS

    @Test
    void consultarEventos_sinAutenticacion_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // GET /api/v1/eventos es público.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/eventos")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    void misEventos_sinAutenticacion_deberiaSerBloqueado() throws Exception {
        // GET /api/v1/eventos/mis requiere autenticación.
        debeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/eventos/mis")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crearEvento_conRolCliente_deberiaRetornarForbidden() throws Exception {
        // Crear eventos requiere ORGANIZADOR o ADMINPLATAFORMA.
        mockMvc.perform(post("/api/v1/eventos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void crearEvento_conRolOrganizador_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // ORGANIZADOR sí puede pasar la seguridad para crear eventos.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(post("/api/v1/eventos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
        );
    }

    // ══════════════════════════════════════════════════════
    // ORGANIZACIONES

    @Test
    void consultarOrganizaciones_sinAutenticacion_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // GET /api/v1/organizaciones es público.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/organizaciones")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crearOrganizacion_conRolCliente_deberiaRetornarForbidden() throws Exception {
        // Crear organización requiere ORGANIZADOR o ADMINPLATAFORMA.
        mockMvc.perform(post("/api/v1/organizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void crearOrganizacion_conRolOrganizador_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // ORGANIZADOR sí puede pasar la seguridad para crear organización.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(post("/api/v1/organizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
        );
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void activarOrganizacion_conRolOrganizador_deberiaRetornarForbidden() throws Exception {
        // Activar organización pendiente es solo ADMINPLATAFORMA.
        mockMvc.perform(put("/api/v1/organizaciones/1/activar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ══════════════════════════════════════════════════════
    // CAUSAS SOCIALES

    @Test
    void causasActivas_sinAutenticacion_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // GET /api/v1/causas/activas es público.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/causas/activas")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crearCausa_conRolCliente_deberiaRetornarForbidden() throws Exception {
        // Crear causa requiere ORGANIZADOR o ADMINPLATAFORMA.
        mockMvc.perform(post("/api/v1/causas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void eliminarCausa_conRolOrganizador_deberiaRetornarForbidden() throws Exception {
        // Desactivar/eliminar causa es solo ADMINPLATAFORMA.
        mockMvc.perform(delete("/api/v1/causas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ══════════════════════════════════════════════════════
    // DONACIONES

    @Test
    void misDonaciones_sinAutenticacion_deberiaSerBloqueado() throws Exception {
        // GET /api/v1/donaciones/me requiere autenticación.
        debeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/donaciones/me")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void misDonaciones_conRolCliente_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // Cualquier usuario autenticado puede ver sus propias donaciones.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/donaciones/me")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void reportesDonaciones_conRolCliente_deberiaRetornarForbidden() throws Exception {
        // Reportes de donaciones son para ADMINPLATAFORMA u ORGANIZADOR.
        mockMvc.perform(get("/api/v1/donaciones/reporte")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ══════════════════════════════════════════════════════
    // NOTIFICACIONES

    @Test
    void contactoNotificaciones_sinAutenticacion_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // Formulario de contacto público.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(post("/api/v1/notificaciones/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
        );
    }

    @Test
    void historialNotificaciones_sinAutenticacion_deberiaSerBloqueado() throws Exception {
        // Historial de notificaciones requiere autenticación.
        debeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/notificaciones/historial/1")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void obtenerNotificacion_conRolCliente_deberiaRetornarForbidden() throws Exception {
        // Obtener notificación por ID es solo ADMINPLATAFORMA.
        mockMvc.perform(get("/api/v1/notificaciones/obtener/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ══════════════════════════════════════════════════════
    // CARRITO

    @Test
    void crearCarrito_sinAutenticacion_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // Crear carrito es ruta pública.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(post("/api/v1/Carrito/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
        );
    }

    @Test
    void listarCarrito_sinAutenticacion_deberiaSerBloqueado() throws Exception {
        // Listar carrito requiere autenticación.
        debeSerBloqueoDeSeguridad(
                mockMvc.perform(get("/api/v1/Carrito/listar")
                        .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void checkoutCarrito_conRolCliente_noDebeSerBloqueadoPorSeguridad() throws Exception {
        // Checkout requiere autenticación, no rol específico.
        noDebeSerBloqueoDeSeguridad(
                mockMvc.perform(post("/api/v1/Carrito/checkout/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
        );
    }
}