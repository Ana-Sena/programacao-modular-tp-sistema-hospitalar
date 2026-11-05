package com.example.TrabalhoSH.api.internacao;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InternacaoResposta> cadastrar(@RequestBody CadastroInternacao entrada) {
        InternacaoResposta resposta = service.cadastrar(entrada);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<InternacaoResposta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public InternacaoResposta buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/situacao")
    public InternacaoResposta alterarSituacao(@PathVariable String id, @RequestBody AlteracaoSituacao entrada) {
        return service.alterarSituacao(id, entrada);
    }
}
