package br.com.fiap.motos_control_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.motos_control_api.dto.MotoDTO;
import br.com.fiap.motos_control_api.model.Localizacao;
import br.com.fiap.motos_control_api.model.Moto;
import br.com.fiap.motos_control_api.repository.LocalizacaoRepository;
import br.com.fiap.motos_control_api.repository.MotoRepository;
import jakarta.validation.Valid;

@Service
public class MotoService {

    @Autowired
    private MotoRepository motoRepository;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    public Page<Moto> findAll(String modelo, Pageable pageable) {
        if (modelo != null && !modelo.isEmpty()) {
            return motoRepository.findByModeloContainingIgnoreCase(modelo, pageable);
        }
        return motoRepository.findAll(pageable);
    }

    public Moto findById(Long id) {
        return motoRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moto não encontrada com ID: " + id));
    }

    public Moto save(@Valid MotoDTO dto) {
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

        return motoRepository.save(moto);
    }

    public Moto update(Long id, @Valid MotoDTO dto) {
        Moto moto = findById(id);

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

        return motoRepository.save(moto);
    }

    public Moto associarLocalizacao(Long idMoto, Long idLocalizacao) {
        Moto moto = findById(idMoto);

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

        return motoRepository.save(moto);
    }

    public void delete(Long id) {
        Moto moto = findById(id);

        if (moto.getLocalizacao() != null) {
            Localizacao localizacao = moto.getLocalizacao();
            localizacao.setMoto(null);
            localizacaoRepository.save(localizacao);
        }

        motoRepository.deleteById(id);
    }
}