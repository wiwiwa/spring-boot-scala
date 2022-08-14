package com.wiwiwa.mill

import mill.T
import mill.scalalib.PublishModule
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}

trait JavaLibraryModule extends ScalaAppModule with PublishModule

trait ScalaLibraryModule extends JavaLibraryModule {
  def organization: String
  def appDescription = "A scala library"
  def appUrl = s"https://github.com/$organization"
  def license = License.MIT

  override def sonatypeUri = super.sonatypeUri.replaceFirst("//","//s01.")
  override def sonatypeSnapshotUri: String = super.sonatypeSnapshotUri.replaceFirst("//","//s01.")
  def pomSettings = T {
    PomSettings(
      description = appDescription,
      organization = organization,
      url = appUrl,
      licenses = Seq(license),
      versionControl = VersionControl(Some(appUrl), None, None, None),
      developers = Seq(Developer("Unknown","Unknown",appUrl))
    )
  }
}
