package org.cmda.management.specifications;

import org.cmda.management.entities.CmdaMember;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CmdaMemberSpecification {

    public static Specification<CmdaMember> withFilters(Long fraternityId, String firstName, String lastName, String profession) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (fraternityId != null) {
                predicates.add(criteriaBuilder.equal(root.get("fraternity").get("id"), fraternityId));
            }

            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }

            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }

            if (profession != null && !profession.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("profession")), "%" + profession.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}