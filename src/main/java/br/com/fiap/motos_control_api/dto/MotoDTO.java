package br.com.fiap.motos_control_api.dto;

import br.com.fiap.motos_control_api.model.Localizacao;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class MotoDTO {

    @NotBlank
    private String identificador;

    @NotBlank
    private String modelo;

    @NotBlank
    private String placa;

    private Localizacao localizacao;

}
