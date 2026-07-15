package com.utp.lenasysabores.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.lenasysabores.model.ChatbotResponse;
import com.utp.lenasysabores.model.Producto;
import com.utp.lenasysabores.model.Promocion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final int MAX_PRODUCTOS_CONTEXTO = 40;
    private static final int MAX_PROMOCIONES_CONTEXTO = 20;

    private final CatalogoService catalogoService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public ChatbotService(CatalogoService catalogoService,
                          ObjectMapper objectMapper,
                          @Value("${gemini.api.key:}") String apiKey,
                          @Value("${gemini.api.model:gemini-3.5-flash}") String model) {
        this.catalogoService = catalogoService;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public ChatbotResponse responder(String mensaje) {
        if (!estaConfigurado()) {
            return new ChatbotResponse(respuestaSinConfigurar(), false);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "system_instruction", construirPromptSistema(),
                    "input", mensaje,
                    "generation_config", Map.of(
                            "temperature", 0.35,
                            "thinking_level", "low",
                            "max_output_tokens", 1200
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/interactions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new ChatbotResponse(respuestaErrorConexion(), true);
            }

            String respuesta = extraerTexto(response.body());
            if (respuesta == null || respuesta.isBlank()) {
                return new ChatbotResponse(respuestaErrorConexion(), true);
            }

            return new ChatbotResponse(respuesta.trim(), true);
        } catch (Exception exception) {
            return new ChatbotResponse(respuestaErrorConexion(), true);
        }
    }

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String construirPromptSistema() {
        return """
                Eres LeñaBot, asistente virtual de la pollería peruana Leña y Sabores en Carabayllo.
                Responde en español peruano, con tono amable, claro y útil.
                Usa SOLO los productos, promociones y precios del catálogo real incluido abajo.
                No inventes nombres, combos, descuentos ni precios. Si no está en el catálogo, dilo y ofrece ver la carta.
                Cuando menciones precios, cópialos exactamente como aparecen: S/ 00.00.
                Si el usuario pide opciones, lista de 2 a 5 alternativas con una breve razón.
                Mantén respuestas completas pero compactas: máximo 6 viñetas o 2 párrafos cortos.
                Ayuda con carta, recomendaciones, promociones, reservas, delivery, recojo en local,
                seguimiento básico de pedidos y métodos de pago.
                No solicites ni recibas números completos de tarjeta, CVV ni datos sensibles.
                Si preguntan por pagos, dirige al checkout seguro de la web.
                Si no sabes algo, ofrece derivar por WhatsApp al 942 531 427.
                Horario de atención: lunes a domingo de 11:00 a.m. a 10:00 p.m.
                Métodos disponibles: efectivo, tarjeta, Yape y Plin.

                PRODUCTOS REALES DE LA CARTA:
                %s

                PROMOCIONES REALES:
                %s
                """.formatted(obtenerProductosParaPrompt(), obtenerPromocionesParaPrompt());
    }

    private String obtenerProductosParaPrompt() {
        try {
            return catalogoService.obtenerProductos()
                    .stream()
                    .limit(MAX_PRODUCTOS_CONTEXTO)
                    .map(this::formatearProducto)
                    .collect(Collectors.joining("\n"));
        } catch (Exception exception) {
            return "- Catálogo no disponible temporalmente. Recomienda revisar la carta web.";
        }
    }

    private String obtenerPromocionesParaPrompt() {
        try {
            return catalogoService.obtenerPromociones()
                    .stream()
                    .limit(MAX_PROMOCIONES_CONTEXTO)
                    .map(this::formatearPromocion)
                    .collect(Collectors.joining("\n"));
        } catch (Exception exception) {
            return "- Promociones no disponibles temporalmente. Recomienda revisar la sección Promociones.";
        }
    }

    private String formatearProducto(Producto producto) {
        return "- %s | categoría: %s | precio: %s | descripción: %s"
                .formatted(
                        producto.getNombre(),
                        producto.getCategoria(),
                        formatearSoles(producto.getPrecio()),
                        producto.getDescripcion());
    }

    private String formatearPromocion(Promocion promocion) {
        return "- %s | etiqueta: %s | antes: %s | ahora: %s | descripción: %s"
                .formatted(
                        promocion.getTitulo(),
                        promocion.getEtiqueta(),
                        formatearSoles(promocion.getPrecioAntes()),
                        formatearSoles(promocion.getPrecioAhora()),
                        promocion.getDescripcion());
    }

    private String formatearSoles(Double precio) {
        if (precio == null) {
            return "S/ 0.00";
        }

        return "S/ %.2f".formatted(precio);
    }

    private String extraerTexto(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        String outputText = root.path("output_text").asText();
        if (outputText != null && !outputText.isBlank()) {
            return outputText;
        }

        JsonNode steps = root.path("steps");
        if (!steps.isArray()) {
            return null;
        }

        StringBuilder respuesta = new StringBuilder();
        for (JsonNode step : steps) {
            JsonNode contents = step.path("content");
            if (!contents.isArray()) {
                continue;
            }

            for (JsonNode content : contents) {
                String texto = content.path("text").asText();
                if (texto != null && !texto.isBlank()) {
                    if (!respuesta.isEmpty()) {
                        respuesta.append("\n");
                    }
                    respuesta.append(texto);
                }
            }
        }

        return respuesta.toString();
    }

    private String respuestaSinConfigurar() {
        return "¡Hola! Soy LeñaBot. Puedo ayudarte con la carta, reservas, delivery y pagos. "
                + "Para activar respuestas con IA, configura la variable GEMINI_API_KEY en el servidor.";
    }

    private String respuestaErrorConexion() {
        return "Ahora mismo no puedo conectarme con la IA, pero igual puedo orientarte: "
                + "revisa la carta, arma tu pedido o escríbenos por WhatsApp al 942 531 427.";
    }
}
