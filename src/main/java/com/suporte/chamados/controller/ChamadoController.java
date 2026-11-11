package com.suporte.chamados.controller;

import com.suporte.chamados.model.Chamado;
import com.suporte.chamados.model.Prioridade;
import com.suporte.chamados.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final List<Chamado> chamados = new ArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public ChamadoController() {
        adicionarDadosIniciais();
    }

    @GetMapping
    public List<Chamado> listarTodos() {
        return List.copyOf(chamados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chamado> buscarPorId(@PathVariable("id") Long id) {
        return chamados.stream()
                .filter(chamado -> chamado.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/setor")
    public List<Chamado> filtrarPorSetor(@RequestParam("nome") String nome) {
        return chamados.stream()
                .filter(chamado -> chamado.getSetor() != null)
                .filter(chamado -> chamado.getSetor().equalsIgnoreCase(nome.trim()))
                .toList();
    }

    @GetMapping("/prioridade")
    public List<Chamado> filtrarPorPrioridade(@RequestParam("nivel") String nivel) {
        String nivelNormalizado = nivel.trim().toUpperCase(Locale.ROOT);

        return chamados.stream()
                .filter(chamado -> chamado.getPrioridade() != null)
                .filter(chamado -> chamado.getPrioridade().name().equals(nivelNormalizado))
                .toList();
    }

    @GetMapping("/busca")
    public List<Chamado> buscarPorTitulo(@RequestParam("texto") String texto) {
        String textoNormalizado = texto.trim().toLowerCase(Locale.ROOT);

        return chamados.stream()
                .filter(chamado -> chamado.getTitulo() != null)
                .filter(chamado -> chamado.getTitulo().toLowerCase(Locale.ROOT).contains(textoNormalizado))
                .toList();
    }

    @GetMapping("/criticos")
    public List<Chamado> listarCriticos() {
        return chamados.stream()
                .filter(chamado -> chamado.getPrioridade() == Prioridade.ALTA)
                .filter(chamado -> chamado.getStatus() != Status.RESOLVIDO)
                .toList();
    }

    @GetMapping("/resumo")
    public Map<String, Long> obterResumo() {
        Map<String, Long> resumo = new LinkedHashMap<>();
        resumo.put("abertos", contarPorStatus(Status.ABERTO));
        resumo.put("emAndamento", contarPorStatus(Status.EM_ANDAMENTO));
        resumo.put("resolvidos", contarPorStatus(Status.RESOLVIDO));
        resumo.put("total", (long) chamados.size());
        return resumo;
    }

    @PostMapping
    public ResponseEntity<Chamado> cadastrar(@RequestBody Chamado chamado) {
        chamado.setId(proximoId.getAndIncrement());
        chamados.add(chamado);
        return ResponseEntity.status(HttpStatus.CREATED).body(chamado);
    }

    private long contarPorStatus(Status status) {
        return chamados.stream()
                .filter(chamado -> chamado.getStatus() == status)
                .count();
    }

    private void adicionarDadosIniciais() {
        adicionar("Impressora não imprime", "Ana", "Financeiro", Prioridade.ALTA, Status.ABERTO);
        adicionar("Computador lento", "Bruno", "Recursos Humanos", Prioridade.MEDIA, Status.EM_ANDAMENTO);
        adicionar("Redefinição de senha", "Carla", "Tecnologia", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar("Sem acesso à internet", "Diego", "Comercial", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar("Instalação de software", "Elisa", "Financeiro", Prioridade.MEDIA, Status.ABERTO);
        adicionar("Conta de e-mail bloqueada", "Fábio", "Marketing", Prioridade.ALTA, Status.RESOLVIDO);
        adicionar("Monitor sem imagem", "Gabriela", "Recursos Humanos", Prioridade.MEDIA, Status.RESOLVIDO);
        adicionar("Erro no sistema de vendas", "Henrique", "Comercial", Prioridade.ALTA, Status.ABERTO);
        adicionar("Teclado com defeito", "Isabela", "Administrativo", Prioridade.BAIXA, Status.EM_ANDAMENTO);
        adicionar("Configuração de impressora", "João", "Administrativo", Prioridade.BAIXA, Status.ABERTO);
    }

    private void adicionar(
            String titulo,
            String solicitante,
            String setor,
            Prioridade prioridade,
            Status status) {
        long id = proximoId.getAndIncrement();
        chamados.add(new Chamado(id, titulo, solicitante, setor, prioridade, status));
    }
}
