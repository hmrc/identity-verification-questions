/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.models

import Utils.UnitSpec
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.identityverificationquestions.models.P60.PaymentToDate

import java.time.{LocalDateTime, ZoneOffset}

class QuestionDataCacheSpec extends UnitSpec {

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
            case _ => fail("expiryDate should be a JsObject")
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
  val questionList = List(QuestionWithAnswers(PaymentToDate,List(TestRecord(1).toString)))
  val dateTime = LocalDateTime.now()
}
