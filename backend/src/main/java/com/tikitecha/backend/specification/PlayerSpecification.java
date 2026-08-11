package com.tikitecha.backend.specification;

import com.tikitecha.backend.model.Player;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class PlayerSpecification {

    public static Specification<Player> hasPosition(Integer positionId) {
        return (root, query, cb) -> cb.equal(root.get("positionId"), positionId);
    }

    public static Specification<Player> hasTeam(Integer teamId) {
        return (root, query, cb) -> cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Player> costLessThanOrEqual(BigDecimal maxCost) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("nowCost"), maxCost);
    }
}