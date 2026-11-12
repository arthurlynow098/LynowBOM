package com.arthurentsev.lynowbom.servico;

import com.arthurentsev.lynowbom.core.ErroEstrutura;
import com.arthurentsev.lynowbom.core.LeitorCsv;
import com.arthurentsev.lynowbom.core.LeitorTabela;
import com.arthurentsev.lynowbom.core.LinhaEstrutura;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lê CSV (UTF-8 ou Windows-1252) e XLSX/XLS, devolvendo as linhas de estrutura. */
@Component
public class LeitorPlanilha {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    public List<LinhaEstrutura> ler(String nomeArquivo, byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new ErroEstrutura("O arquivo enviado está vazio.");
        }
        String nome = nomeArquivo == null ? "" : nomeArquivo.toLowerCase(Locale.ROOT);
        try {
            if (nome.endsWith(".xlsx") || nome.endsWith(".xls") || pareceZip(conteudo)) {
                return lerExcel(conteudo);
            }
            return LeitorCsv.ler(new StringReader(decodificar(conteudo)));
        } catch (IOException e) {
            throw new ErroEstrutura("Não foi possível ler o arquivo: " + e.getMessage());
        }
    }

    private static boolean pareceZip(byte[] b) {
        return b.length > 3 && b[0] == 'P' && b[1] == 'K' && b[2] == 3 && b[3] == 4;
    }

    /** Excel em português costuma salvar CSV em Windows-1252; tenta UTF-8 estrito antes. */
    static String decodificar(byte[] conteudo) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(conteudo))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(conteudo, WINDOWS_1252);
        }
    }

    private List<LinhaEstrutura> lerExcel(byte[] conteudo) throws IOException {
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(conteudo))) {
            Sheet aba = wb.getSheet("estrutura");
            if (aba == null) {
                aba = wb.getSheetAt(0);
            }
            DataFormatter formatador = new DataFormatter(Locale.ROOT);
            List<List<String>> tabela = new ArrayList<>();
            int ultima = aba.getLastRowNum();
            for (int r = aba.getFirstRowNum(); r <= ultima; r++) {
                Row linha = aba.getRow(r);
                List<String> campos = new ArrayList<>();
                if (linha != null) {
                    for (int c = 0; c < Math.max(linha.getLastCellNum(), 0); c++) {
                        campos.add(texto(linha.getCell(c), formatador));
                    }
                }
                tabela.add(campos);
            }
            // Remove linhas vazias antes do cabeçalho, comuns em planilhas com título.
            while (!tabela.isEmpty() && tabela.get(0).stream().allMatch(String::isBlank)) {
                tabela.remove(0);
            }
            return LeitorTabela.interpretar(tabela);
        }
    }

    private static String texto(Cell celula, DataFormatter formatador) {
        if (celula == null) {
            return "";
        }
        CellType tipo = celula.getCellType() == CellType.FORMULA ? celula.getCachedFormulaResultType() : celula.getCellType();
        if (tipo == CellType.NUMERIC && !DateUtil.isCellDateFormatted(celula)) {
            // Evita o formato regional: 1,35 e 1.35 viram sempre "1.35".
            return BigDecimal.valueOf(celula.getNumericCellValue()).stripTrailingZeros().toPlainString();
        }
        if (tipo == CellType.STRING) {
            return celula.getStringCellValue();
        }
        return formatador.formatCellValue(celula);
    }
}
