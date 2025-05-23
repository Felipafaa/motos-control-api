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
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.motos_control_api.dto.MotoDTO;
import br.com.fiap.motos_control_api.model.Localizacao;
import br.com.fiap.motos_control_api.model.Moto;
import br.com.fiap.motos_control_api.repository.LocalizacaoRepository;
import br.com.fiap.motos_control_api.repository.MotoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/motos")
@RequiredArgsConstructor
public class MotoController {

    @Autowired
    private final MotoRepository motoRepository;

    @Autowired
    private final LocalizacaoRepository localizacaoRepository;

    @GetMapping
    public ResponseEntity<Page<Moto>> findAll(
            @RequestParam(defaultValue = "") String modelo,
            @PageableDefault(size = 10, sort = "modelo") Pageable pageable) {
        return ResponseEntity.ok(motoRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Moto> findById(@PathVariable Long id) {
        return motoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Moto> save(@Valid @RequestBody MotoDTO dto) {
        Moto moto = new Moto();
        moto.setIdentificador(dto.getIdentificador());
        moto.setModelo(dto.getModelo());
        moto.setPlaca(dto.getPlaca());

        if (dto.getLocalizacao() != null) {
            Localizacao localizacao = dto.getLocalizacao();
            localizacao.setMoto(moto);
            moto.setLocalizacao(localizacao);
        }

        return ResponseEntity.ok(motoRepository.save(moto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Moto> update(@PathVariable Long id, @Valid @RequestBody MotoDTO dto) {
        return motoRepository.findById(id).map(moto -> {
            moto.setIdentificador(dto.getIdentificador());
            moto.setModelo(dto.getModelo());
            moto.setPlaca(dto.getPlaca());
            return ResponseEntity.ok(motoRepository.save(moto));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{idMoto}/localizacao/{idLocalizacao}")
    public ResponseEntity<Moto> associarLocalizacao(
            @PathVariable Long idMoto,
            @PathVariable Long idLocalizacao) {

        Moto moto = motoRepository.findById(idMoto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moto não encontrada"));

        Localizacao localizacao = localizacaoRepository.findById(idLocalizacao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Localização não encontrada"));

        moto.setLocalizacao(localizacao);

        return ResponseEntity.ok(motoRepository.save(moto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!motoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        motoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
