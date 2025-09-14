package br.com.fiap.motos_control_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import br.com.fiap.motos_control_api.dto.MotoDTO;
import br.com.fiap.motos_control_api.model.Moto;
import br.com.fiap.motos_control_api.service.MotoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/motos")
public class MotoController {

    @Autowired
    private MotoService motoService;

    @GetMapping
    public ResponseEntity<Page<Moto>> findAll(
            @RequestParam(required = false, defaultValue = "") String modelo,
            @PageableDefault(size = 10, sort = "modelo") Pageable pageable) {
        return ResponseEntity.ok(motoService.findAll(modelo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Moto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(motoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Moto> save(@Valid @RequestBody MotoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(motoService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Moto> update(@PathVariable Long id, @Valid @RequestBody MotoDTO dto) {
        return ResponseEntity.ok(motoService.update(id, dto));
    }

    @PutMapping("/{idMoto}/localizacao/{idLocalizacao}")
    public ResponseEntity<Moto> associarLocalizacao(
            @PathVariable Long idMoto,
            @PathVariable Long idLocalizacao) {
        return ResponseEntity.ok(motoService.associarLocalizacao(idMoto, idLocalizacao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        motoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
