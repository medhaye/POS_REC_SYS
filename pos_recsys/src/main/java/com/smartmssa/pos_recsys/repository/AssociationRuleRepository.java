package com.smartmssa.pos_recsys.repository;

import com.smartmssa.pos_recsys.entity.AssociationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssociationRuleRepository extends JpaRepository<AssociationRule, Long> {
}
