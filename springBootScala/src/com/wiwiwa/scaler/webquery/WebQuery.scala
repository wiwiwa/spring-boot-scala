package com.wiwiwa.scaler.webquery

import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.jpa.repository.query.QueryUtils
import org.springframework.data.support.PageableExecutionUtils

import java.util.Date
import jakarta.persistence.criteria.{CriteriaQuery, Predicate, Selection}
import jakarta.persistence.{Entity, EntityManager, MappedSuperclass}
import jakarta.servlet.http.HttpServletRequest

import java.lang.reflect.Field
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
    val fields = allEntityAttributes(entityClass).map{f=>f.getName->f.getType}.toMap
    val predicates: List[Predicate] = req.getParameterMap.entrySet.asScala.iterator
      .filter(_.getKey.head!='$')
      .map { e=>
        val paramName = e.getKey
        val paramValue = e.getValue()(0)
        paramName.last match
          case '*' =>
            if paramName.head == '*' then
              val fieldName = paramName.drop(1).dropRight(1)
              fields.get(fieldName) match
                case Some(c) if c==classOf[String] => cb.like(root.get(fieldName), '%' + paramValue + '%')
                case _ => throw new IllegalArgumentException(s"Invalid field name or value for field: $fieldName")
            else
              val fieldName = paramName.dropRight(1)
              fields.get(fieldName) match
                case Some(c) if c==classOf[String] => cb.like(root.get(fieldName), paramValue + '%')
                case _ => throw new IllegalArgumentException(s"Invalid field name or value for field: $fieldName")
          case '>' =>
            val fieldName = paramName.dropRight(1)
            fields.get(fieldName) match
              case Some(c)  if classOf[Number].isAssignableFrom(c) => cb.greaterThanOrEqualTo(root.get(fieldName), paramValue.toLong)
              case Some(c)  if classOf[Date].isAssignableFrom(c) => cb.greaterThanOrEqualTo(root.get(fieldName), new Date(paramValue.toLong))
              case _ => throw new IllegalArgumentException(s"Invalid field name or value for field: $fieldName")
          case '<' =>
            val fieldName = paramName.dropRight(1)
            fields.get(fieldName) match
              case Some(c)  if classOf[Number].isAssignableFrom(c) => cb.lessThanOrEqualTo(root.get(fieldName), paramValue.toLong)
              case Some(c)  if classOf[Date].isAssignableFrom(c) => cb.lessThanOrEqualTo(root.get(fieldName), new Date(paramValue.toLong))
              case _ => throw new IllegalArgumentException(s"Invalid field name or value for field: $fieldName")
          case _ =>
            if paramName.head == '*' then
              val fieldName = paramName.substring(1)
              fields.get(fieldName) match
                case Some(c) if c==classOf[String] => cb.like(root.get(fieldName), '%' + paramValue)
                case _ => throw new IllegalArgumentException(s"Invalid field name or value for field: $fieldName")
            else fields.get(paramName) match
              case Some(c) =>
                val value = if c.isEnum then toEnumOrdinal(c,paramValue) else paramValue
                cb.equal(root.get(paramName), value)
              case _ => throw new IllegalArgumentException(s"Invalid field name or value for field: $paramName")
      }.toList
    if predicates.nonEmpty then
      query.where(predicates:_*)
    if pageable==null then  //count for all records
      query.select( cb.count(root).asInstanceOf[Selection[R]] )
    else if pageable.getSort.isSorted then
        query.orderBy(QueryUtils.toOrders(pageable.getSort, root, cb))
    query

  def allEntityAttributes(clazz:Class[_]): Array[Field] =
    if clazz==null then Array.empty
    else if clazz.isAnnotationPresent(classOf[Entity]) || clazz.isAnnotationPresent(classOf[MappedSuperclass]) then
      clazz.getDeclaredFields ++ allEntityAttributes(clazz.getSuperclass)
    else Array.empty
  def toEnumOrdinal(clazz:Class[_], enumValue:String): Int =
    val e = clazz.getMethod("valueOf", classOf[String]).invoke(null,enumValue)
    e.asInstanceOf[scala.reflect.Enum].ordinal