package br.com.fiap.motos_control_api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.motos_control_api.dto.LocalizacaoDTO;
import br.com.fiap.motos_control_api.model.Localizacao;
import br.com.fiap.motos_control_api.model.Moto;
import br.com.fiap.motos_control_api.repository.LocalizacaoRepository;
import br.com.fiap.motos_control_api.repository.MotoRepository;

@Service
public class LocalizacaoService {

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    @Autowired
    private MotoRepository motoRepository;

    public Page<Localizacao> findAll(String zona, Pageable pageable) {
        if (zona != null && !zona.isEmpty()) {
            return localizacaoRepository.findByZonaContainingIgnoreCase(zona, pageable);
        }
        return localizacaoRepository.findAll(pageable);
    }

    public Localizacao findById(Long id) {
        return localizacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Localização não encontrada com ID: " + id));
    }

    public Localizacao criar(LocalizacaoDTO localizacaoDTO) {
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

        return localizacaoRepository.save(localizacao);
    }

    public Localizacao update(Long id, LocalizacaoDTO dto) {
        Localizacao localizacao = findById(id);

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

        return localizacaoRepository.save(localizacao);
    }

    public Localizacao associarMoto(Long idLocalizacao, Long idMoto) {
        Localizacao localizacao = findById(idLocalizacao);

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

        return localizacaoRepository.save(localizacao);
    }

    public void delete(Long id) {
        Localizacao localizacao = findById(id);

        if (localizacao.getMoto() != null) {
            Moto motoAssociada = localizacao.getMoto();
            motoAssociada.setLocalizacao(null);
            motoRepository.save(motoAssociada);
        }

        localizacaoRepository.delete(localizacao);
    }

    public List<Localizacao> findAvailable() {
        return localizacaoRepository.findByMotoIsNull();
    }
}
