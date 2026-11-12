package com.arthurentsev.lynowbom.servico;

import com.arthurentsev.lynowbom.dominio.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** No perfil {@code demo}, carrega a planilha de exemplo quando o banco está vazio. */
@Component
@Profile("demo")
public class CargaDemonstracao implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaDemonstracao.class);

    private final CatalogoService catalogo;
    private final ItemRepository itens;

    public CargaDemonstracao(CatalogoService catalogo, ItemRepository itens) {
        this.catalogo = catalogo;
        this.itens = itens;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (itens.count() > 0) {
            return;
        }
        byte[] conteudo = new ClassPathResource("exemplos/estrutura-exemplo.csv").getContentAsByteArray();
        CatalogoService.ResumoImportacao r = catalogo.importar("estrutura-exemplo.csv", conteudo);
        log.info("Demonstração carregada: {} itens, produtos {}", r.itensNovos(), r.produtos());
    }
}
