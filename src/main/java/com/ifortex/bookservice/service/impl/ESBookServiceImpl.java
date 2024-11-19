package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
				.collect(Collectors.toMap(result -> (String) ((Object[]) result)[0],
						result -> (Long) ((Object[]) result)[1], (oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}

	@Override
	public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<Book> cr = cb.createQuery(Book.class);
		Root<Book> root = cr.from(Book.class);

		List<Predicate> predicate = new ArrayList<>();

		String title = searchCriteria.getTitle();
		if (title != null) {
			title = title.trim();
			predicate.add(cb.like(root.get("title"), String.format("%%%s%%", title)));
		}

		String author = searchCriteria.getAuthor();
		if (author != null) {
			author = author.trim();
			predicate.add(cb.like(root.get("author"), String.format("%%%s%%", author)));
		}

		String genre = searchCriteria.getGenre();
		if (genre != null) {
			predicate.add(cb.like(
					cb.concat("|", cb.concat(
							cb.function("array_to_string", String.class, root.get("genres"), cb.literal("|")), "|")),
					cb.literal(String.format("%%|%s|%%", genre))));
		}

		String description = searchCriteria.getDescription();
		if (description != null) {
			description = description.trim();
			predicate.add(cb.like(root.get("description"), String.format("%%%s%%", description)));
		}

		Integer year = searchCriteria.getYear();
		if (year != null) {
			LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
			LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);
			predicate.add(cb.between(root.get("publicationDate"), startOfYear, endOfYear));
		}

		cr.select(root).where(cb.and(predicate.toArray(new Predicate[0])));
		return em.createQuery(cr).getResultList();
	}
}
