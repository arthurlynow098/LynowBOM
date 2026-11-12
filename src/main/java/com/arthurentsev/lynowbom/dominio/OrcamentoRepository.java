package com.arthurentsev.lynowbom.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoRepository extends JpaRepository<OrcamentoEntity, Long> {

    List<OrcamentoEntity> findTop50ByOrderByCriadoEmDesc();
}
