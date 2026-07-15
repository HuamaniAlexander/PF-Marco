package com.utp.lenasysabores.controller;

import com.utp.lenasysabores.model.ChatbotRequest;
import com.utp.lenasysabores.model.ChatbotResponse;
import com.utp.lenasysabores.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private static final int MAX_MENSAJE_LENGTH = 600;

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/mensaje")
    public ResponseEntity<ChatbotResponse> responder(@RequestBody ChatbotRequest request) {
        String mensaje = request != null ? request.getMensaje() : null;

        if (mensaje == null || mensaje.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatbotResponse("Escríbeme una consulta para poder ayudarte.", chatbotService.estaConfigurado()));
        }

        if (mensaje.length() > MAX_MENSAJE_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(new ChatbotResponse("Tu mensaje es muy largo. Resúmelo un poquito y lo vemos juntos.", chatbotService.estaConfigurado()));
        }

        return ResponseEntity.ok(chatbotService.responder(mensaje.trim()));
    }
}
