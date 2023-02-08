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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import org.joda.time.{Days, LocalDate}
import play.api.libs.json._
import play.api.mvc.Request
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.MongoAnswerConnector
import uk.gov.hmrc.identityverificationquestions.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SAAnswerConnector @Inject()(appConfig: AppConfig, questionRepo: QuestionMongoRepository, auditService: AuditService)
                                 (implicit ec: ExecutionContext) extends MongoAnswerConnector(questionRepo, auditService) {

  override def checkResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails)(implicit request: Request[_]): Score = {

    answerDetails.questionKey match {
      case SelfAssessedIncomeFromPensionsQuestion => handleSAIncomeFromPensionsQuestion(questionDataCaches, answerDetails)
      case SelfAssessedPaymentQuestion => handleSAPaymentQuestion(questionDataCaches, answerDetails)
    }
  }

  private def handleSAIncomeFromPensionsQuestion(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails) = {
    val offset: Int = appConfig.saAnswerOffset
    val intAnswer: BigInt = BigInt(answerDetails.answer.toString)
    questionDataCaches.flatMap(qdc => qdc.questions.filter(_.questionKey == answerDetails.questionKey)
      .flatMap(_.answers)).map(BigDecimal(_)).map(_.toBigInt()).count(a => a - offset <= intAnswer && a + offset >= intAnswer) match {
      case 0 => Incorrect
      case _ => Correct
    }
  }

  private def handleSAPaymentQuestion(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails) = {

    val userAnswer: Option[SAPayment] = convertToSaPayment(Json.parse(answerDetails.answer.toString))
    val payments: Seq[SAPayment] = questionDataCaches.flatMap(qdc => qdc.questions.filter(_.questionKey == answerDetails.questionKey)
      .flatMap(_.answers)).map(Json.parse).flatMap(convertToSaPayment)

    if(userAnswer.isDefined) {
      payments.exists(payment => {
        (payment.paymentDate, userAnswer.get.paymentDate) match {
          case (Some(paymentDate), Some(answerDate))
            if payment.amount == userAnswer.get.amount => insidePaymentToleranceWindow(answerDate, paymentDate)
          case _ => false
        }
      }
      ) match {
        case true => Correct
        case _ => Incorrect
      }
    } else Incorrect

  }

  private def insidePaymentToleranceWindow(dateEntered: LocalDate, expectedDate: LocalDate): Boolean = {
    val diff = Days.daysBetween(dateEntered, expectedDate).getDays
    diff >= (0 - appConfig.saPaymentTolerancePastDays) && diff <= appConfig.saPaymentToleranceFutureDays
  }

  def convertToSaPayment(value: JsValue): Option[SAPayment] = ((value \ "amount"), (value \ "paymentDate")) match {
    case (JsDefined(JsNumber(amountString)), JsDefined(JsString(paymentDateString))) =>
      Some(SAPayment(amountString, Some(LocalDate.parse(paymentDateString))))
    case _ => None
  }
}

