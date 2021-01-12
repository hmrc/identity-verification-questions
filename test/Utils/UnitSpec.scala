/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package Utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, ResultExtractors, Writeables}

import scala.concurrent.Future

trait UnitSpec
  extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with HeaderNames
    with Status
    with MimeTypes
    with DefaultAwaitTimeout
    with ResultExtractors
    with Writeables
    with FutureAwaits {

  def contentAsHtml(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  def status(result: Result): Int = status(Future.successful(result))

  def contentAsString(result: Result): String = contentAsString(Future.successful(result))

  def contentAsJson(result: Result): JsValue = contentAsJson(Future.successful(result))

  def contentAsHtml(result: Result): Document = contentAsHtml(Future.successful(result))

  def redirectLocation(result: Result): Option[String] = redirectLocation(Future.successful(result))

  def header(headerName: String, result: Result): Option[String] = header(headerName, Future.successful(result))
}
