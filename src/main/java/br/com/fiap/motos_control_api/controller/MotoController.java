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
            if (dto.getLocalizacao().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ID da Localização é obrigatório para associação e não pode ser nulo quando o objeto Localizacao é fornecido.");
            }
            Long localizacaoId = dto.getLocalizacao().getId();
            Localizacao localizacaoExistente = localizacaoRepository.findById(localizacaoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Localização com ID " + localizacaoId + " não encontrada para associar à moto."));

            if (localizacaoExistente.getMoto() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Localização com ID " + localizacaoId
                        + " já está associada a outra moto (ID: " + localizacaoExistente.getMoto().getId() + ").");
            }

            localizacaoExistente.setMoto(moto);
            moto.setLocalizacao(localizacaoExistente);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(motoRepository.save(moto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Moto> update(@PathVariable Long id, @Valid @RequestBody MotoDTO dto) {
        Moto moto = motoRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moto não encontrada com ID: " + id));

        moto.setIdentificador(dto.getIdentificador());
        moto.setModelo(dto.getModelo());
        moto.setPlaca(dto.getPlaca());

        Localizacao localizacaoAtualDaMoto = moto.getLocalizacao();

        if (dto.getLocalizacao() != null) {
            if (dto.getLocalizacao().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ID da Localização é obrigatório para associação e não pode ser nulo quando o objeto Localizacao é fornecido.");
            }
            Long idLocalizacaoDesejada = dto.getLocalizacao().getId();
            Localizacao localizacaoDesejada = localizacaoRepository.findById(idLocalizacaoDesejada)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Localização com ID " + idLocalizacaoDesejada + " não encontrada para associar à moto."));

            if (localizacaoDesejada.getMoto() != null && !localizacaoDesejada.getMoto().getId().equals(moto.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Localização com ID " + localizacaoDesejada.getId() + " já está associada a outra moto (ID: "
                                + localizacaoDesejada.getMoto().getId() + ").");
            }

            if (localizacaoAtualDaMoto != null && !localizacaoAtualDaMoto.getId().equals(localizacaoDesejada.getId())) {
                localizacaoAtualDaMoto.setMoto(null);
            }

            localizacaoDesejada.setMoto(moto);
            moto.setLocalizacao(localizacaoDesejada);

        } else {
            if (localizacaoAtualDaMoto != null) {
                localizacaoAtualDaMoto.setMoto(null);
                moto.setLocalizacao(null);
            }
        }

        return ResponseEntity.ok(motoRepository.save(moto));
    }

    @PutMapping("/{idMoto}/localizacao/{idLocalizacao}")
    public ResponseEntity<Moto> associarLocalizacao(
            @PathVariable Long idMoto,
            @PathVariable Long idLocalizacao) {

        Moto moto = motoRepository.findById(idMoto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Moto não encontrada com ID: " + idMoto));

        Localizacao localizacaoParaAssociar = localizacaoRepository.findById(idLocalizacao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Localização não encontrada com ID: " + idLocalizacao));

        if (localizacaoParaAssociar.getMoto() != null && !localizacaoParaAssociar.getMoto().getId().equals(idMoto)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Localização com ID " + idLocalizacao
                    + " já está associada a outra moto (ID: " + localizacaoParaAssociar.getMoto().getId() + ").");
        }

        Localizacao localizacaoAtualDaMoto = moto.getLocalizacao();
        
        if (localizacaoAtualDaMoto != null && !localizacaoAtualDaMoto.getId().equals(idLocalizacao)) {
            localizacaoAtualDaMoto.setMoto(null);
        }

        moto.setLocalizacao(localizacaoParaAssociar);
        localizacaoParaAssociar.setMoto(moto);

        return ResponseEntity.ok(motoRepository.save(moto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Moto moto = motoRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moto não encontrada com ID: " + id));

        if (moto.getLocalizacao() != null) {
            Localizacao localizacao = moto.getLocalizacao();
            localizacao.setMoto(null);
            localizacaoRepository.save(localizacao);
        }

        motoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
