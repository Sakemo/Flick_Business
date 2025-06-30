package br.com.king.flick_business.repository.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.king.flick_business.entity.Produto;
import jakarta.persistence.criteria.Predicate;

public class ProdutoSpecification {
    public static Specification<Produto> withFilter(String name, Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            root.fetch("categoria", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("fornecedor", jakarta.persistence.criteria.JoinType.LEFT);
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoria").get("id"), categoryId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
