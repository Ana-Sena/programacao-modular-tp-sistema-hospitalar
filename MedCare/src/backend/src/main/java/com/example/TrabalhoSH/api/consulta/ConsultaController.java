package com.example.TrabalhoSH.api.consulta;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConsultaResposta> cadastrar(@RequestBody CadastroConsulta entrada) {
        ConsultaResposta resposta = service.cadastrar(entrada);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<ConsultaResposta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ConsultaResposta buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/situacao")
    public ConsultaResposta alterarSituacao(@PathVariable String id, @RequestBody AlteracaoSituacao entrada) {
        return service.alterarSituacao(id, entrada);
    }
}
