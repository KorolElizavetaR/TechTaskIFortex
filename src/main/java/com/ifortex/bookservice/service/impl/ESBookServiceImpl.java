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
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();
		Root<Book> book = query.from(Book.class);

		Expression<String> genreUnnest = cb.function("unnest", String.class, book.get("genres"));
		Expression<Long> bookCount = cb.count(book);

		query.multiselect(genreUnnest, bookCount).groupBy(genreUnnest).orderBy(cb.desc(bookCount));

		List<Tuple> resultList = em.createQuery(query).getResultList();

		Map<String, Long> genreCounts = resultList.stream()
				.collect(Collectors.toMap(tuple -> tuple.get(0, String.class), tuple -> tuple.get(1, Long.class)));

		return genreCounts;
	}

	@Override
	public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {
		// will be implemented shortly
		return List.of();
	}
}
