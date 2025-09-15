package br.com.fiap.motos_control_api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.motos_control_api.model.Localizacao;

public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {

    Page<Localizacao> findByZonaContainingIgnoreCase(String zona, Pageable pageable);

    List<Localizacao> findByMotoIsNull();
}
