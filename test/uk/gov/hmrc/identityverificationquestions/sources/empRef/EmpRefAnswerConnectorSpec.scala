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

package uk.gov.hmrc.identityverificationquestions.sources.empRef

import Utils.UnitSpec
import org.joda.time.DateTime
import play.api.Configuration
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.PayeRefQuestion.{AmountOfPayment, DateOfPayment}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.{Duration, Instant, LocalDateTime, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global

class EmpRefAnswerConnectorSpec extends UnitSpec {

  "checkPayeResult" should {
    "return Score Correct if AmountOfPayment is right, and DateOfPayment is within payeeAmountOfDaysLeewayForPaymentDate(4)" in new Setup {

      val correlationId: CorrelationId = CorrelationId()
      val selection: Selection = Selection(EmpRef("123", "1234567"))
      val questionSeq: Seq[QuestionWithAnswers] = Seq(QuestionWithAnswers(DateOfPayment, List("2022-01-31")), QuestionWithAnswers(AmountOfPayment, List("2000")))

      val questionDataCache: QuestionDataCache =
        QuestionDataCache(
          correlationId,
          selection,
          questionSeq,
          Instant.now().plus(Duration.ofMinutes(1)))

      val answerDetails1: AnswerDetails =  AnswerDetails(DateOfPayment, SimpleAnswer("2022-01-29"))
      val answerDetails2: AnswerDetails =  AnswerDetails(DateOfPayment, SimpleAnswer("2022-01-31"))
      val answerDetails3: AnswerDetails =  AnswerDetails(DateOfPayment, SimpleAnswer("2022-02-01"))
      val answerDetails4: AnswerDetails =  AnswerDetails(DateOfPayment, SimpleAnswer("2022-02-03"))
      val answerDetails5: AnswerDetails =  AnswerDetails(DateOfPayment, SimpleAnswer("2022-02-04"))

      empRefAnswerConnector.checkPayeResult(Seq(questionDataCache), answerDetails1) shouldBe Correct
      empRefAnswerConnector.checkPayeResult(Seq(questionDataCache), answerDetails2) shouldBe Correct
      empRefAnswerConnector.checkPayeResult(Seq(questionDataCache), answerDetails3) shouldBe Correct
      empRefAnswerConnector.checkPayeResult(Seq(questionDataCache), answerDetails4) shouldBe Correct
      empRefAnswerConnector.checkPayeResult(Seq(questionDataCache), answerDetails5) shouldBe Incorrect
    }

  }

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockQuestionMongoRepository: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
    val mockAuditService: AuditService = mock[AuditService]

    val configData: Map[String, Any] = Map(
      "microservice.services.desPayeService.payeeAmountOfDaysLeewayForPaymentDate" -> 4
    )
    val config: Configuration = Configuration.from(configData)
    val servicesConfig: ServicesConfig = new ServicesConfig(config)
    val appConfig : AppConfig = new AppConfig(config, servicesConfig)

    val testDate: DateTime = DateTime.parse("2020-06-01")

    val empRefAnswerConnector = new EmpRefAnswerConnector(mockQuestionMongoRepository, mockAuditService, appConfig)
  }
}
