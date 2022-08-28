package com.wiwiwa.scaler.webquery

import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.jpa.repository.query.QueryUtils
import org.springframework.data.support.PageableExecutionUtils

import javax.persistence.criteria.{CriteriaQuery, Predicate, Selection}
import javax.persistence.EntityManager
import javax.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters.*

trait WebQuery[T]:
  def execute(): Page[T]

class WebQueryImpl[T](req:HttpServletRequest, page:Pageable, entityClass:Class[T], entityManager:EntityManager) extends WebQuery[T]:
  override def execute() =
    val data =
      val query = entityManager.createQuery( createQuery(entityClass, page) )
      if page.isPaged then
        query.setFirstResult(page.getOffset.toInt)
          .setMaxResults(page.getPageSize)
      query.getResultList
    val total = entityManager.createQuery( createQuery(classOf[Long], null))
      .getResultList.asScala
      .sum
    PageableExecutionUtils.getPage(data, page, ()=>total)

  def createQuery[R](resultType:Class[R], pageable:Pageable): CriteriaQuery[R] =
    val cb = entityManager.getCriteriaBuilder
    val query = cb.createQuery(resultType)
    val root = query.from(entityClass)
    val predicates: List[Predicate] = req.getParameterMap.entrySet.asScala.iterator
      .map { e=>
        val paramName = e.getKey
        val paramValue = e.getValue()(0)
        paramName.last match
          case '*' =>
            if paramName.head == '*' then
              val fieldName = paramName.drop(1).dropRight(1)
              entityClass.getDeclaredField(fieldName) match
                case f if f == null || f.getType != classOf[String] => null
                case _ => cb.like(root.get(fieldName), '%' + paramValue + '%')
            else
              val fieldName = paramName.dropRight(1)
              entityClass.getDeclaredField(fieldName) match
                case f if f == null || f.getType != classOf[String] => null
                case _ => cb.like(root.get(fieldName), paramValue + '%')
          case '>' =>
            val fieldName = paramName.dropRight(1)
            entityClass.getDeclaredField(fieldName) match
              case f if f == null || !classOf[Number].isAssignableFrom(f.getType) => null
              case _ => cb.greaterThanOrEqualTo(root.get(fieldName), paramValue.toLong)
          case '<' =>
            val fieldName = paramName.dropRight(1)
            entityClass.getDeclaredField(fieldName) match
              case f if f == null || !classOf[Number].isAssignableFrom(f.getType) => null
              case _ => cb.lessThanOrEqualTo(root.get(fieldName), paramValue.toLong)
          case _ =>
            if paramName.head == '*' then
              val fieldName = paramName.substring(1)
              cb.like(root.get(fieldName), '%' + paramValue)
            else cb.equal(root.get(paramName), paramValue)
      }.filter(_ != null)
      .toList
    query.where(predicates:_*)
    if pageable==null then  //count for all records
      query.select( cb.count(root).asInstanceOf[Selection[R]] )
    else if pageable.getSort.isSorted then
        query.orderBy(QueryUtils.toOrders(pageable.getSort, root, cb))
    query
