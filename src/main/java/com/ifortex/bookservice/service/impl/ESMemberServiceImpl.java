package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Attention! It is FORBIDDEN to make any changes in this file!
/* Same commentary as in ESBookServiceImpl */

@Service
@Transactional
@RequiredArgsConstructor
public class ESMemberServiceImpl implements MemberService {
	@PersistenceContext
	private final EntityManager em;

	@Override
	public Member findMember() {
		String genre = "Romance";
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Member> cq = cb.createQuery(Member.class);
		Root<Member> memberRoot = cq.from(Member.class);
		Join<Member, Book> joinMemberBook = memberRoot.join("borrowedBooks", JoinType.INNER);

		Subquery<Long> genreQuery = cq.subquery(Long.class);
		Root<Book> bookRoot = genreQuery.from(Book.class);
		genreQuery.select(bookRoot.get("id"))
				.where(cb.like(cb.concat("|", cb.concat(
						cb.function("array_to_string", String.class, bookRoot.get("genres"), cb.literal("|")), "|")),
						cb.literal(String.format("%%|%s|%%", genre))));

		List<Order> orders = new ArrayList<>();
		orders.add(cb.asc(joinMemberBook.get("publicationDate")));
		orders.add(cb.desc(memberRoot.get("membershipDate")));

		cq.select(memberRoot).where(cb.in(joinMemberBook.get("id")).value(genreQuery)).orderBy(orders);
		return em.createQuery(cq).setMaxResults(1).getSingleResult();
	}

	@Override
	public List<Member> findMembers() {
		// will be implemented shortly
		return List.of();
	}
}
