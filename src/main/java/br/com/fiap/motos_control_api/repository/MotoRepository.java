package br.com.fiap.motos_control_api.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.motos_control_api.model.Moto;

public interface MotoRepository extends JpaRepository<Moto, Long>{

    @Cacheable("motosPorModelo")
    Page<Moto> findByModeloContainingIgnoreCase(String modelo, Pageable pageable);
}
