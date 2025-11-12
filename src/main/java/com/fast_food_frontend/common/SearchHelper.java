package com.fast_food_frontend.common;

import io.github.perplexhub.rsql.RSQLJPASupport;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

public interface SearchHelper {
    static <T> Specification<T> parseSearchToken(String search, List<String> searchFields) {
        return search != null && !search.isBlank() && searchFields != null && !searchFields.isEmpty() ? (Specification)searchFields.stream().map((field) -> field + "=like='" + search.trim() + "'").collect(Collectors.collectingAndThen(Collectors.joining(","), RSQLJPASupport::toSpecification)) : RSQLJPASupport.toSpecification((String)null);
    }
}
