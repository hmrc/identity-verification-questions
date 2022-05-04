/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.questionrepository.models.P60.PaymentToDate

import java.time.{LocalDateTime, ZoneOffset}
import scala.util.{Failure, Success, Try}

class QuestionDataCacheSpec extends UnitSpec{

  "when creating a questionDataCache it " should {
    "allow valid inputs" in new Setup {
      val questionDataCache: QuestionDataCache = QuestionDataCache(correlationId, selection, questionList, dateTime)
      questionDataCache.selection shouldBe selection
      questionDataCache.questions shouldBe questionList
    }
  }

  "Json format" should {
    "contain a BSON date type for expiryDate to support TTL" in new Setup {

      val questionDataCache: QuestionDataCache = QuestionDataCache(correlationId, selection, questionList, dateTime)

      val maybeJsValue: Option[JsValue] = (JsPath \ "expiryDate").apply(Json.toJson(questionDataCache)).headOption

      maybeJsValue match {
        case Some(expiryDateJsValue) =>
          expiryDateJsValue match {
            case JsObject(underlying) => underlying.get("$date") match { // BSON type
              case Some(JsObject(value)) => value.get("$numberLong") match { // BSON type
                case Some(JsString(epochMilliString)) =>
                  epochMilliString.toLong shouldBe dateTime.toInstant(ZoneOffset.UTC).toEpochMilli
                case Some(other) => fail(s"Found $other instead of JsString")
                case None => fail("No valid $numberLong epoch milli value found")
              }
              case None => fail("No $date key with object found in date field")
              case jsValue => fail(s"Found wrong type of value for expiryDate: $jsValue")
            }
          }
        case None => fail("No expiryDate field found in document")
      }

    }
  }

}

trait Setup {
  val correlationId = CorrelationId()
  val ninoIdentifier: Nino = Nino("AA000000D")
  val selection: Selection = Selection(ninoIdentifier)
  case class TestRecord(value: BigDecimal)
  val questionList = List(Question(PaymentToDate,List(TestRecord(1).toString)))
  val dateTime = LocalDateTime.now()
}
