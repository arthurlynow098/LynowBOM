"use strict";

const $ = (id) => document.getElementById(id);
const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const numero = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 4 });
const data = new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" });

const dinheiro = (v) => (v == null ? "—" : moeda.format(Number(v)));
const qtd = (v) => numero.format(Number(v));

async function api(caminho, opcoes = {}) {
  const resposta = await fetch(caminho, opcoes);
  const corpo = resposta.headers.get("content-type")?.includes("json") ? await resposta.json() : null;
  if (!resposta.ok) {
    const erro = new Error(corpo?.detail || `Erro ${resposta.status}`);
    erro.detalhes = corpo?.detalhes || [];
    throw erro;
  }
  return corpo;
}

function avisar(texto, detalhes = [], erro = false) {
  const el = $("aviso");
  el.className = erro ? "aviso erro" : "aviso";
  el.replaceChildren(document.createTextNode(texto));
  if (detalhes.length) {
    const ul = document.createElement("ul");
    detalhes.slice(0, 12).forEach((d) => {
      const li = document.createElement("li");
      li.textContent = d;
      ul.append(li);
    });
    el.append(ul);
  }
  el.hidden = false;
}

function celula(texto, classe) {
  const td = document.createElement("td");
  td.textContent = texto;
  if (classe) td.className = classe;
  return td;
}

function pedidoAtual() {
  return {
    produto: $("produto").value,
    quantidade: Number($("quantidade").value),
    cliente: $("cliente").value || null,
    margemPercentual: Number($("margem").value || 0),
    impostosPercentual: Number($("impostos").value || 0),
    frete: Number($("frete").value || 0),
  };
}

/** Mesma fórmula do servidor, para a prévia do carimbo enquanto se digita. */
function previa(custo, p) {
  const margem = Math.round(custo * p.margemPercentual) / 100;
  const base = custo + margem + p.frete;
  const total = Math.round((base / (1 - p.impostosPercentual / 100)) * 100) / 100;
  return { margem, impostos: total - base, total, unitario: total / p.quantidade };
}

let ultimaExplosao = null;

function desenharExplosao(r) {
  ultimaExplosao = r;
  const arvore = $("arvore");
  arvore.replaceChildren(...r.arvore.map((n) => {
    const li = document.createElement("li");
    li.dataset.nivel = n.nivel;
    li.className = n.tipo === "MONTAGEM" ? "montagem" : "";
    const no = document.createElement("span");
    no.className = "no";
    no.style.setProperty("--nivel", n.nivel);
    const codigo = document.createElement("span");
    codigo.className = "codigo";
    codigo.textContent = n.codigo;
    no.append(codigo, document.createTextNode(n.descricao));
    const q = document.createElement("span");
    q.className = "qtd";
    q.textContent = qtd(n.quantidadeAcumulada);
    li.append(no, q);
    return li;
  }));

  const linhas = [];
  const grupo = (titulo, itens, total) => {
    itens.forEach((m) => {
      const tr = document.createElement("tr");
      tr.append(celula(m.codigo), celula(m.descricao), celula(qtd(m.quantidade), "num"),
        celula(m.unidade || ""), celula(dinheiro(m.custoUnitario), "num"), celula(dinheiro(m.custoTotal), "num"));
      linhas.push(tr);
    });
    const tr = document.createElement("tr");
    tr.className = "subtotal";
    const td = celula(titulo);
    td.colSpan = 5;
    tr.append(td, celula(dinheiro(total), "num"));
    linhas.push(tr);
  };
  grupo("Materiais", r.materiais, r.custoMateriais);
  if (r.montagens.some((m) => Number(m.custoTotal) > 0)) {
    grupo("Processos de montagem", r.montagens.filter((m) => Number(m.custoTotal) > 0), r.custoProcessos);
  }
  $("tabela").replaceChildren(...linhas);
  $("avisos").replaceChildren(...r.avisos.map((a) => {
    const li = document.createElement("li");
    li.textContent = a;
    return li;
  }));
  atualizarCarimbo();
}

function atualizarCarimbo(emitido) {
  if (!ultimaExplosao) return;
  const r = ultimaExplosao;
  const p = pedidoAtual();
  const v = emitido
    ? { margem: emitido.valorMargem, impostos: emitido.valorImpostos, total: emitido.precoTotal, unitario: emitido.precoUnitario }
    : previa(Number(r.custoMateriais) + Number(r.custoProcessos), p);
  const desc = r.arvore[0]?.descricao || r.produto;
  $("c-produto").textContent = `${r.produto} · ${desc} · ${qtd(r.quantidadePedida)} un`;
  $("c-materiais").textContent = dinheiro(r.custoMateriais);
  $("c-processos").textContent = dinheiro(r.custoProcessos);
  $("c-margem").textContent = dinheiro(v.margem);
  $("c-impostos").textContent = dinheiro(v.impostos);
  $("c-frete").textContent = dinheiro(emitido ? emitido.frete : p.frete);
  $("c-unitario").textContent = dinheiro(v.unitario);
  $("c-total").textContent = dinheiro(v.total);
  $("c-numero").textContent = emitido ? `Nº ${emitido.id}${emitido.cliente ? " · " + emitido.cliente : ""}` : "Prévia";
  $("c-data").textContent = data.format(emitido ? new Date(emitido.criadoEm) : new Date());
}

let espera;
function explodirComAtraso() {
  clearTimeout(espera);
  espera = setTimeout(explodir, 200);
}

async function explodir() {
  const p = pedidoAtual();
  if (!p.produto || !(p.quantidade > 0)) return;
  try {
    desenharExplosao(await api(`api/produtos/${encodeURIComponent(p.produto)}/explosao?quantidade=${p.quantidade}`));
  } catch (e) {
    avisar(e.message, e.detalhes, true);
  }
}

async function carregarProdutos(selecionar) {
  const produtos = await api("api/produtos");
  const select = $("produto");
  select.replaceChildren(...produtos.map((p) => new Option(`${p.codigo} — ${p.descricao}`, p.codigo)));
  if (selecionar) select.value = selecionar;
  const vazio = produtos.length === 0;
  $("form").hidden = vazio;
  $("sem-produtos").hidden = !vazio;
  if (!vazio) await explodir();
}

async function carregarHistorico() {
  const lista = await api("api/orcamentos");
  $("historico").replaceChildren(...lista.map((o) => {
    const tr = document.createElement("tr");
    const baixar = document.createElement("td");
    const link = document.createElement("a");
    link.href = `api/orcamentos/${o.id}/materiais.csv`;
    link.textContent = "CSV";
    baixar.append(link);
    tr.append(celula(o.id), celula(data.format(new Date(o.criadoEm))), celula(o.cliente || "—"),
      celula(o.produto), celula(qtd(o.quantidade), "num"), celula(dinheiro(o.precoTotal), "num"), baixar);
    return tr;
  }));
}

$("form").addEventListener("input", (e) => {
  if (e.target.id === "produto" || e.target.id === "quantidade") explodirComAtraso();
  else atualizarCarimbo();
});
$("produto").addEventListener("change", explodir);

$("form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const o = await api("api/orcamentos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(pedidoAtual()),
    });
    atualizarCarimbo(o);
    avisar(`Orçamento nº ${o.id} emitido: ${dinheiro(o.precoTotal)}.`);
    await carregarHistorico();
  } catch (err) {
    avisar(err.message, err.detalhes, true);
  }
});

$("arquivo").addEventListener("change", async (e) => {
  const arquivo = e.target.files[0];
  if (!arquivo) return;
  const dados = new FormData();
  dados.append("arquivo", arquivo);
  try {
    const r = await api("api/importacoes", { method: "POST", body: dados });
    avisar(`${r.arquivo}: ${r.itensNovos} itens novos, ${r.itensAtualizados} atualizados e ${r.componentes} ligações de estrutura.`);
    await carregarProdutos(r.produtos[0]);
  } catch (err) {
    avisar(`Planilha não importada. ${err.message}`, err.detalhes, true);
  } finally {
    e.target.value = "";
  }
});

carregarProdutos().then(carregarHistorico).catch((e) => avisar(e.message, e.detalhes, true));
