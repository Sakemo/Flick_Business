package br.com.king.flick_business.repository.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.king.flick_business.entity.Product;
import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {
    public static Specification<Product> withFilter(String name, Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            root.fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("provider", jakarta.persistence.criteria.JoinType.LEFT);
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
