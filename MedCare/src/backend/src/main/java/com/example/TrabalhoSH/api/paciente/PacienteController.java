package com.example.TrabalhoSH.api.paciente;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteResposta> cadastrar(@RequestBody CadastroPaciente entrada) {
        PacienteResposta resposta = service.cadastrar(entrada);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<PacienteResposta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PacienteResposta buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public PacienteResposta atualizar(@PathVariable String id, @RequestBody AtualizacaoPaciente entrada) {
        return service.atualizar(id, entrada);
    }
}
