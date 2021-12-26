package com.wiwiwa.mill.spring.test

import org.springframework.data.jpa.repository.JpaRepository

import java.util.Date
import javax.persistence.{Entity, Id}

@Entity
class DemoBean {
  @Id
  var id = 1
  var msg = s"hello at ${new Date()}"
}

trait DemoBeanRepository extends JpaRepository[DemoBean, Int]
