package br.com.fiap.motos_control_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.motos_control_api.dto.LocalizacaoDTO;
import br.com.fiap.motos_control_api.model.Localizacao;
import br.com.fiap.motos_control_api.model.Moto;
import br.com.fiap.motos_control_api.repository.LocalizacaoRepository;
import br.com.fiap.motos_control_api.repository.MotoRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/localizacoes")
@RequiredArgsConstructor
public class LocalizacaoController {

    @Autowired
    private final LocalizacaoRepository localizacaoRepository;

    @Autowired
    private final MotoRepository motoRepository;

    @GetMapping
    @Cacheable("localizacoes")
    public ResponseEntity<List<Localizacao>> findAll() {
        return ResponseEntity.ok(localizacaoRepository.findAll());
    }

    @GetMapping("/zona/{zona}")
    public ResponseEntity<List<Localizacao>> findByZona(@PathVariable String zona) {
        return ResponseEntity.ok(localizacaoRepository.findByZona(zona));
    }

    @PostMapping
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> criar(@RequestBody LocalizacaoDTO localizacaoDTO) {
        Localizacao localizacao = new Localizacao();
        localizacao.setZona(localizacaoDTO.getZona());

        if (localizacaoDTO.getMoto() != null && localizacaoDTO.getMoto().getId() != null) {
            Moto moto = motoRepository.findById(localizacaoDTO.getMoto().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moto não encontrada"));
            localizacao.setMoto(moto);
        }

        localizacaoRepository.save(localizacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(localizacao);
    }

    @PutMapping("/{idLocalizacao}/moto/{idMoto}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> associarLocalizacao(
            @PathVariable Long idLocalizacao,
            @PathVariable Long idMoto) {

        Localizacao localizacao = localizacaoRepository.findById(idLocalizacao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Localização não encontrada"));

        Moto moto = motoRepository.findById(idMoto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moto não encontrada"));

        localizacao.setMoto(moto);

        return ResponseEntity.ok(localizacaoRepository.save(localizacao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!localizacaoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        localizacaoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
