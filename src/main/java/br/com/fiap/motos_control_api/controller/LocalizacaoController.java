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
import jakarta.validation.Valid;
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
    public ResponseEntity<Localizacao> criar(@Valid @RequestBody LocalizacaoDTO localizacaoDTO) {
        Localizacao localizacao = new Localizacao();
        localizacao.setZona(localizacaoDTO.getZona());

        if (localizacaoDTO.getMoto() != null) {
            if (localizacaoDTO.getMoto().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ID da Moto é obrigatório para associação e não pode ser nulo quando o objeto Moto é fornecido.");
            }
            Long motoId = localizacaoDTO.getMoto().getId();
            Moto motoExistente = motoRepository.findById(motoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Moto com ID " + motoId + " não encontrada para associar à localização."));

            if (motoExistente.getLocalizacao() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Moto com ID " + motoId + " já possui uma localização associada (ID: "
                                + motoExistente.getLocalizacao().getId()
                                + "). Não é permitido alterar a localização da moto por esta operação.");
            }

            localizacao.setMoto(motoExistente);
            motoExistente.setLocalizacao(localizacao);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(localizacaoRepository.save(localizacao));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> update(@PathVariable Long id, @Valid @RequestBody LocalizacaoDTO dto) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Localização não encontrada com ID: " + id));

        localizacao.setZona(dto.getZona());

        Moto motoAtualDaLocalizacao = localizacao.getMoto();
        Moto motoParaAssociar = null;

        if (dto.getMoto() != null) {
            if (dto.getMoto().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ID da Moto é obrigatório para associação quando o objeto Moto é fornecido.");
            }
            Long idMotoDesejada = dto.getMoto().getId();
            motoParaAssociar = motoRepository.findById(idMotoDesejada)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Moto com ID " + idMotoDesejada + " não encontrada para associar à localização."));

            if (motoParaAssociar.getLocalizacao() != null
                    && !motoParaAssociar.getLocalizacao().getId().equals(localizacao.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Moto com ID " + motoParaAssociar.getId() + " já está associada a outra localização (ID: "
                                + motoParaAssociar.getLocalizacao().getId() + ").");
            }
        }

        if (motoAtualDaLocalizacao != null &&
                (motoParaAssociar == null || !motoAtualDaLocalizacao.getId().equals(motoParaAssociar.getId()))) {

            motoAtualDaLocalizacao.setLocalizacao(null);
            motoRepository.save(motoAtualDaLocalizacao);
        }

        if (motoParaAssociar != null) {
            motoParaAssociar.setLocalizacao(localizacao);
            motoRepository.save(motoParaAssociar);
        }

        localizacao.setMoto(motoParaAssociar);

        return ResponseEntity.ok(localizacaoRepository.save(localizacao));
    }

    @PutMapping("/{idLocalizacao}/moto/{idMoto}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Localizacao> associarLocalizacao(
            @PathVariable Long idLocalizacao,
            @PathVariable Long idMoto) {

        Localizacao localizacao = localizacaoRepository.findById(idLocalizacao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Localização não encontrada com ID: " + idLocalizacao));

        Moto motoParaAssociar = motoRepository.findById(idMoto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Moto não encontrada com ID: " + idMoto));

        if (localizacao.getMoto() != null && !localizacao.getMoto().getId().equals(idMoto)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Localização com ID " + idLocalizacao
                    + " já está associada a outra moto (ID: " + localizacao.getMoto().getId() + ").");
        }

        if (motoParaAssociar.getLocalizacao() != null
                && !motoParaAssociar.getLocalizacao().getId().equals(idLocalizacao)) {
            Localizacao localizacaoAntigaDaMoto = motoParaAssociar.getLocalizacao();
            localizacaoAntigaDaMoto.setMoto(null);
        }

        localizacao.setMoto(motoParaAssociar);
        motoParaAssociar.setLocalizacao(localizacao);

        motoRepository.save(motoParaAssociar);

        return ResponseEntity.ok(localizacaoRepository.save(localizacao));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "localizacoes", allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Localização não encontrada com ID: " + id));

        if (localizacao.getMoto() != null) {
            Moto motoAssociada = localizacao.getMoto();
            motoAssociada.setLocalizacao(null);
            motoRepository.save(motoAssociada);
        }

        localizacaoRepository.delete(localizacao);
        return ResponseEntity.noContent().build();
    }
}
