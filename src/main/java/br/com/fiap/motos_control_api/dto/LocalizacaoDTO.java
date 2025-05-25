package br.com.fiap.motos_control_api.dto;

import br.com.fiap.motos_control_api.model.Moto;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LocalizacaoDTO {

    @NotBlank
    private String zona;

    private Moto moto;

}
