package com.wiwiwa.mill

import mill.scalalib.ScalaModule

trait ScalaAppModule extends ScalaModule with JavaAppModule {
  /** default scala version */
  override def scalaVersion = "3.1.0"
}
