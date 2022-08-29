package com.wiwiwa.mill.spring.test

import org.springframework.data.jpa.repository.JpaRepository

import java.util.Date
import javax.persistence.{Entity, Id}

@Entity
class DemoEntiy:
  @Id
  var id: java.lang.Long = null
  var msg: String = null
  var lastModified: Date = null

trait DemoBeanRepository extends JpaRepository[DemoEntiy, Int]
