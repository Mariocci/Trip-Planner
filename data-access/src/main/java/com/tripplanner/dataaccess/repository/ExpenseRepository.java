package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Trosak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface ExpenseRepository extends JpaRepository<Trosak, Integer> {

    
    List<Trosak> findByPutovanje_PutovanjeId(Integer putovanjeId);

    
    @Query("SELECT SUM(t.iznos) FROM Trosak t WHERE t.putovanje.putovanjeId = :putovanjeId")
    BigDecimal sumByPutovanjeId(@Param("putovanjeId") Integer putovanjeId);
}
