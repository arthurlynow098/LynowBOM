package com.arthurentsev.lynowbom.web;

import com.arthurentsev.lynowbom.core.ErroEstrutura;
import com.arthurentsev.lynowbom.core.Estrutura;
import com.arthurentsev.lynowbom.core.Explosao;
import com.arthurentsev.lynowbom.dominio.OrcamentoEntity;
import com.arthurentsev.lynowbom.servico.CatalogoService;
import com.arthurentsev.lynowbom.servico.OrcamentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    public record ItemDto(String codigo, String descricao, String unidade, BigDecimal custoUnitario,
                          boolean montagem, int componentes) {
    }

    public record PedidoOrcamento(
            @NotBlank String produto,
            @NotNull @DecimalMin(value = "0.000001", message = "deve ser maior que zero") BigDecimal quantidade,
            @Size(max = 120) String cliente,
            @DecimalMin("0") BigDecimal margemPercentual,
            @DecimalMin("0") BigDecimal impostosPercentual,
            @DecimalMin("0") BigDecimal frete) {
    }

    public record OrcamentoResumo(Long id, Instant criadoEm, String cliente, String produto, String descricaoProduto,
                                  BigDecimal quantidade, BigDecimal precoTotal, BigDecimal precoUnitario) {
        static OrcamentoResumo de(OrcamentoEntity o) {
            return new OrcamentoResumo(o.getId(), o.getCriadoEm(), o.getCliente(), o.getProduto(),
                    o.getDescricaoProduto(), o.getQuantidade(), o.getPrecoTotal(), o.getPrecoUnitario());
        }
    }

    private final CatalogoService catalogo;
    private final OrcamentoService orcamentos;

    public ApiController(CatalogoService catalogo, OrcamentoService orcamentos) {
        this.catalogo = catalogo;
        this.orcamentos = orcamentos;
    }

    @PostMapping(value = "/importacoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoService.ResumoImportacao importar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            throw new ErroEstrutura("Selecione uma planilha .csv ou .xlsx.");
        }
        return catalogo.importar(arquivo.getOriginalFilename(), arquivo.getBytes());
    }

    @GetMapping("/itens")
    public List<ItemDto> itens() {
        Estrutura e = catalogo.estrutura();
        return e.itens().stream()
                .sorted(Comparator.comparing(Estrutura.Item::codigo))
                .map(i -> new ItemDto(i.codigo(), i.descricao(), i.unidade(), i.custoUnitario(),
                        e.ehMontagem(i.codigo()), e.componentesDe(i.codigo()).size()))
                .toList();
    }

    @GetMapping("/produtos")
    public List<ItemDto> produtos() {
        Estrutura e = catalogo.estrutura();
        return e.produtos().stream()
                .map(i -> new ItemDto(i.codigo(), i.descricao(), i.unidade(), i.custoUnitario(), true,
                        e.componentesDe(i.codigo()).size()))
                .toList();
    }

    @GetMapping("/produtos/{codigo}/explosao")
    public Explosao.Resultado explodir(@PathVariable String codigo,
                                       @RequestParam(defaultValue = "1") BigDecimal quantidade) {
        return catalogo.explodir(codigo, quantidade);
    }

    @PostMapping("/orcamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoEntity emitir(@Valid @RequestBody PedidoOrcamento p) {
        return orcamentos.emitir(new OrcamentoService.Pedido(p.produto(), p.quantidade(), p.cliente(),
                p.margemPercentual(), p.impostosPercentual(), p.frete()));
    }

    @GetMapping("/orcamentos")
    public List<OrcamentoResumo> recentes() {
        return orcamentos.recentes().stream().map(OrcamentoResumo::de).toList();
    }

    @GetMapping("/orcamentos/{id}")
    public OrcamentoEntity orcamento(@PathVariable Long id) {
        return orcamentos.buscar(id);
    }

    @GetMapping("/orcamentos/{id}/materiais.csv")
    public ResponseEntity<byte[]> csv(@PathVariable Long id) {
        byte[] corpo = orcamentos.csv(id).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("orcamento-" + id + "-materiais.csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }

    @GetMapping("/modelo.csv")
    public ResponseEntity<byte[]> modelo() throws IOException {
        byte[] corpo = new ClassPathResource("exemplos/estrutura-exemplo.csv").getContentAsByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("estrutura-modelo.csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(corpo);
    }
}
