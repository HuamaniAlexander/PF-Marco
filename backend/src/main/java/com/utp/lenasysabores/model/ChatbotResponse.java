package com.utp.lenasysabores.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatbotResponse {

    private String respuesta;
    private boolean configurado;

    public ChatbotResponse(String respuesta, boolean configurado) {
        this.respuesta = respuesta;
        this.configurado = configurado;
    }
}
