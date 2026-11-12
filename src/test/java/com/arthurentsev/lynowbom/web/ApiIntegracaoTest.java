package com.arthurentsev.lynowbom.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sobe a aplicação no perfil demo (H2 + planilha de exemplo) e percorre a API. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ApiIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listaProdutosDaDemonstracao() throws Exception {
        mvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItem("QDC-12")));
    }

    @Test
    void explodeProduto() throws Exception {
        mvc.perform(get("/api/produtos/qdc-12/explosao").param("quantidade", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produto").value("QDC-12"))
                .andExpect(jsonPath("$.custoMateriais").value(9795.70));
    }

    @Test
    void emiteOrcamentoEExportaCsv() throws Exception {
        String corpo = """
                {"produto":"QDC-12","quantidade":10,"cliente":"Teste","margemPercentual":25,
                 "impostosPercentual":18,"frete":150}
                """;
        String resposta = mvc.perform(post("/api/orcamentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precoTotal").value(18545.28))
                .andReturn().getResponse().getContentAsString();
        String id = resposta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/orcamentos/" + id + "/materiais.csv"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Preço total;;;;18545,28")));
    }

    @Test
    void recusaPlanilhaQueCriaCiclo() throws Exception {
        byte[] csv = "pai;codigo;descricao;quantidade\nCHAPA-14;QDC-12;Ciclo;1\n".getBytes(StandardCharsets.UTF_8);
        mvc.perform(multipart("/api/importacoes").file(new MockMultipartFile("arquivo", "ciclo.csv", "text/csv", csv)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("ciclo")));
    }

    @Test
    void validaPedido() throws Exception {
        mvc.perform(post("/api/orcamentos").contentType(MediaType.APPLICATION_JSON).content("{\"produto\":\"\",\"quantidade\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.length()").value(2));
    }
}
