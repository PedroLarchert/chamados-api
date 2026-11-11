package com.suporte.chamados.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChamadoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarOsDezChamadosIniciais() throws Exception {
        mockMvc.perform(get("/chamados"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Impressora não imprime"));
    }

    @Test
    void deveConsultarChamadoPorId() throws Exception {
        mockMvc.perform(get("/chamados/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Impressora não imprime"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    void deveRetornarNaoEncontradoParaIdInexistente() throws Exception {
        mockMvc.perform(get("/chamados/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveFiltrarSetorSemDiferenciarMaiusculasDeMinusculas() throws Exception {
        mockMvc.perform(get("/chamados/setor").param("nome", "fInAnCeIrO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].setor", everyItem(is("Financeiro"))));
    }

    @Test
    void deveFiltrarPrioridadeSemDiferenciarMaiusculasDeMinusculas() throws Exception {
        mockMvc.perform(get("/chamados/prioridade").param("nivel", "aLtA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].prioridade", everyItem(is("ALTA"))));
    }

    @Test
    void deveBuscarNoTituloSemDiferenciarMaiusculasDeMinusculas() throws Exception {
        mockMvc.perform(get("/chamados/busca").param("texto", "IMPRESSORA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Impressora não imprime"))
                .andExpect(jsonPath("$[1].titulo").value("Configuração de impressora"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaCorrespondencia() throws Exception {
        mockMvc.perform(get("/chamados/busca").param("texto", "projetor inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveListarSomenteChamadosAltosAindaNaoResolvidos() throws Exception {
        mockMvc.perform(get("/chamados/criticos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].prioridade", everyItem(is("ALTA"))))
                .andExpect(jsonPath("$[*].status", everyItem(not(is("RESOLVIDO")))))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(4))
                .andExpect(jsonPath("$[2].id").value(8));
    }

    @Test
    void deveRetornarQuantidadePorStatusETotal() throws Exception {
        mockMvc.perform(get("/chamados/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abertos").value(4))
                .andExpect(jsonPath("$.emAndamento").value(3))
                .andExpect(jsonPath("$.resolvidos").value(3))
                .andExpect(jsonPath("$.total").value(10));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deveCadastrarChamadoComIdGeradoERetornarCreated() throws Exception {
        String novoChamado = """
                {
                  "id": 999,
                  "titulo": "Notebook não liga",
                  "solicitante": "Marina",
                  "setor": "Jurídico",
                  "prioridade": "ALTA",
                  "status": "ABERTO"
                }
                """;

        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(novoChamado))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.titulo").value("Notebook não liga"))
                .andExpect(jsonPath("$.solicitante").value("Marina"))
                .andExpect(jsonPath("$.setor").value("Jurídico"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.status").value("ABERTO"));

        mockMvc.perform(get("/chamados/{id}", 11))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.titulo").value("Notebook não liga"));
    }
}
