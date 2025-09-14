package br.com.fiap.motos_control_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.motos_control_api.dto.LocalizacaoDTO;
import br.com.fiap.motos_control_api.model.Localizacao;
import br.com.fiap.motos_control_api.service.LocalizacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/localizacoes")
public class LocalizacaoController {

    @Autowired
    private LocalizacaoService localizacaoService;

    @GetMapping
    @Cacheable("localizacoes")
    public ResponseEntity<Page<Localizacao>> findAll(
            @RequestParam(required = false, defaultValue = "") String zona,
            @PageableDefault(size = 10, sort = "zona") Pageable pageable) {
        return ResponseEntity.ok(localizacaoService.findAll(zona, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Localizacao> findById(@PathVariable Long id) {
        return ResponseEntity.ok(localizacaoService.findById(id));
    }

    @PostMapping
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> criar(@Valid @RequestBody LocalizacaoDTO localizacaoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(localizacaoService.criar(localizacaoDTO));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> update(@PathVariable Long id, @Valid @RequestBody LocalizacaoDTO dto) {
        return ResponseEntity.ok(localizacaoService.update(id, dto));
    }

    @PutMapping("/{idLocalizacao}/moto/{idMoto}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> associarMoto(
            @PathVariable Long idLocalizacao,
            @PathVariable Long idMoto) {
        return ResponseEntity.ok(localizacaoService.associarMoto(idLocalizacao, idMoto));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        localizacaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
