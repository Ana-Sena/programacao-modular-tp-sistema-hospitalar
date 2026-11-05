package com.example.TrabalhoSH.controller;

import model.Cirurgiao;
import model.ClinicoGeral;
import model.Especialista;
import model.ProfissionalSaude;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalSaudeController {

    private final List<ProfissionalSaude> profissionais = new ArrayList<>();

    @GetMapping
    public List<ProfissionalSaude> listarProfissionais() {
        return profissionais;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalSaude cadastrarProfissional(@RequestBody ProfissionalSaudeRequest request) {
        ProfissionalSaude profissional = criarProfissional(request);
        profissionais.add(profissional);
        return profissional;
    }

    private ProfissionalSaude criarProfissional(ProfissionalSaudeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O corpo da requisição é obrigatório.");
        }

        if (request.getTipo() == null || request.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo 'tipo' é obrigatório.");
        }

        String tipo = request.getTipo().trim().toLowerCase(Locale.ROOT).replaceAll("[\\s_-]", "");
        ProfissionalSaude profissional = switch (tipo) {
            case "clinicogeral", "clínicogeral" -> new ClinicoGeral();
            case "especialista" -> new Especialista();
            case "cirurgiao", "cirurgião" -> new Cirurgiao();
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de profissional inválido. Use 'clinicoGeral', 'especialista' ou 'cirurgiao'."
            );
        };

        profissional.setNome(request.getNome());
        profissional.setRegistroProfissional(request.getRegistroProfissional());
        profissional.setEspecialidade(request.getEspecialidade());
        profissional.setTelefone(request.getTelefone());
        profissional.setEmail(request.getEmail());

        return profissional;
    }
}