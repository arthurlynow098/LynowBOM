package com.arthurentsev.lynowbom.dominio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ComponenteRepository extends JpaRepository<ComponenteEntity, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ComponenteEntity c where c.pai in :pais")
    int removerDosPais(@Param("pais") Collection<String> pais);
}
