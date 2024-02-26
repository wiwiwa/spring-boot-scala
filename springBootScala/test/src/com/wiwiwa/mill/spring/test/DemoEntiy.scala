package com.wiwiwa.mill.spring.test

import org.springframework.data.jpa.repository.JpaRepository

import java.util.Date
import jakarta.persistence.{Entity, Id, MappedSuperclass}

@MappedSuperclass
class BaseEntityClass:
  var baseValue = "baseValue"

@Entity
class DemoEntiy extends BaseEntityClass:
  @Id
  var id: java.lang.Long = null
  var msg: String = null
  var lastModified: Date = null
  var status = Status.VALID

trait DemoBeanRepository extends JpaRepository[DemoEntiy, Int]

enum Status extends Enum[Status]:
  case VALID, REVOKED