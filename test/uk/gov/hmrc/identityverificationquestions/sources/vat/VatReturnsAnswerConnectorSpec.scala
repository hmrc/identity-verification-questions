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

package uk.gov.hmrc.identityverificationquestions.sources.vat

import Utils.UnitSpec
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Vat.{ValueOfPurchasesAmount, ValueOfSalesAmount}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global

class VatReturnsAnswerConnectorSpec extends UnitSpec {

  "checkVatResult" should {

    "return the correct score for when the answers are correct" in new Setup {

      val correlationId: CorrelationId = CorrelationId()
      val selection: Selection = Selection(Vrn("123456789"))
      val questionSeq: Seq[QuestionWithAnswers] = Seq(QuestionWithAnswers(ValueOfSalesAmount, Seq("1000")), QuestionWithAnswers(ValueOfPurchasesAmount, Seq("500.50")))

      val questionDataCache: QuestionDataCache =
        QuestionDataCache(
          correlationId,
          selection,
          questionSeq,
          LocalDateTime.now(ZoneOffset.UTC) plusMinutes 1)

      val answerDetails1: AnswerDetails = AnswerDetails(ValueOfSalesAmount, SimpleAnswer("999.50"))
      val answerDetails2: AnswerDetails = AnswerDetails(ValueOfSalesAmount, SimpleAnswer("999.51"))
      val answerDetails3: AnswerDetails = AnswerDetails(ValueOfSalesAmount, SimpleAnswer("1000"))
      val answerDetails4: AnswerDetails = AnswerDetails(ValueOfSalesAmount, SimpleAnswer("1000.50"))
      val answerDetails5: AnswerDetails = AnswerDetails(ValueOfSalesAmount, SimpleAnswer("1000.51"))
      val answerDetails6: AnswerDetails = AnswerDetails(ValueOfSalesAmount, SimpleAnswer("1001"))

      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails1) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails2) shouldBe Correct
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails3) shouldBe Correct
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails4) shouldBe Correct
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails5) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails6) shouldBe Incorrect


      val answerDetails7: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("499.50"))
      val answerDetails8: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("499.51"))
      val answerDetails9: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("500"))
      val answerDetails10: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("500.50"))
      val answerDetails11: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("500.51"))
      val answerDetails12: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("500.99"))
      val answerDetails13: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("501"))
      val answerDetails14: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("501.50"))
      val answerDetails15: AnswerDetails = AnswerDetails(ValueOfPurchasesAmount, SimpleAnswer("501.51"))

      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails7) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails8) shouldBe Correct
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails9) shouldBe Correct
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails10) shouldBe Correct
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails11) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails12) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails13) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails14) shouldBe Incorrect
      vatReturnsAnswerConnector.checkVatResult(Seq(questionDataCache), answerDetails15) shouldBe Incorrect

    }

    trait Setup {

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val mockQuestionMongoRepository: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
      val mockAuditService: AuditService = mock[AuditService]

      val vatReturnsAnswerConnector = new VatReturnsAnswerConnector(mockQuestionMongoRepository, mockAuditService)

    }

  }
}
