package br.com.fiap.motos_control_api.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Moto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String identificador;

    @NotBlank
    private String modelo;

    @NotBlank
    private String placa;

    @Builder.Default
    private boolean ativa = true;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "localizacao_id", referencedColumnName = "id")
    @JsonManagedReference
    private Localizacao localizacao;

}