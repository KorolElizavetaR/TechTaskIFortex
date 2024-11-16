package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* Attention! It is FORBIDDEN to make any changes in this file! 
 * 	I might be wrong, but maybe this comment belongs to Controller's classes?*/

@Transactional
@RequiredArgsConstructor
@Service
public class ESBookServiceImpl implements BookService {
	@PersistenceContext
	private final EntityManager em;

	@Override
	public Map<String, Long> getBooks() {
		String nativeSQL = """
			    SELECT genre_dist AS genre, COUNT(*) AS counter
			    FROM books, UNNEST(genre) AS genre_dist
			    GROUP BY genre_dist
			    ORDER BY counter DESC
			""";

	return (Map<String, Long>) em.createNativeQuery(nativeSQL).getResultList().stream()
			.collect(Collectors.toMap(
					result -> (String) ((Object[]) result)[0],
					result -> (Long) ((Object[]) result)[1], 
					(oldValue, newValue) -> oldValue, 
					LinkedHashMap::new));
	}

	@Override
	public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {
		// will be implemented shortly
		return List.of();
	}
}
