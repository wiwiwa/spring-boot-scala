package com.wiwiwa.mill

import mill.eval.Evaluator
import mill.scalalib.{Dependency, ScalaModule}

trait ScalaAppModule extends ScalaModule with JavaAppModule {
  /** default scala version */
  override def scalaVersion = "3.1.0"

  def showUpdates(ev: Evaluator) = Dependency.showUpdates(ev)
}
