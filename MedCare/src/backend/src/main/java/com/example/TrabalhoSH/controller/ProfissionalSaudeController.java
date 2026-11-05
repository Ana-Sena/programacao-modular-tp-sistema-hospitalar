package com.example.TrabalhoSH.controller;

import com.example.TrabalhoSH.model.ProfissionalSaude;
import com.example.TrabalhoSH.service.ProfissionalSaudeService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalSaudeController {
    private final ProfissionalSaudeService profissionais;

    public ProfissionalSaudeController(ProfissionalSaudeService profissionais) {
        this.profissionais = profissionais;
    }

    @GetMapping
    public List<ProfissionalSaude> listarProfissionais() {
        return profissionais.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalSaude cadastrarProfissional(@RequestBody ProfissionalSaudeRequest request) {
        return profissionais.cadastrar(request);
    }
}
