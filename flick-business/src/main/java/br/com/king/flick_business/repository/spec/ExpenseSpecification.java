package br.com.king.flick_business.repository.spec;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.enums.TipoExpense;
import jakarta.persistence.criteria.Predicate;

public class ExpenseSpecification {
    public static Specification<Expense> withFilter(
            String name, ZonedDateTime start, ZonedDateTime end, TipoExpense tipoExpense) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataExpense"), start));
            }

            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataExpense"), end));
            }

            if (tipoExpense != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoExpense"), tipoExpense));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
