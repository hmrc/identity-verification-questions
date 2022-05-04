/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.{Inject, Singleton}
import org.joda.time.{Days, LocalDate}
import play.api.libs.json.{JsDefined, JsError, JsNumber, JsResult, JsString, JsSuccess, JsValue, Json}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.MongoAnswerConnector
import uk.gov.hmrc.questionrepository.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext

@Singleton
class SAAnswerConnector @Inject()(appConfig: AppConfig, questionRepo: QuestionMongoRepository)
                                 (implicit ec: ExecutionContext) extends MongoAnswerConnector(questionRepo) {

  override def checkResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails): Score = {

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

