/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.{Inject, Singleton}
import org.joda.time.{Days, LocalDate}
import play.api.libs.json.{JsResult, Json}
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
    val payments: Seq[SAPayment] = questionDataCaches.flatMap(qdc => qdc.questions.filter(_.questionKey == answerDetails.questionKey)
      .flatMap(_.answers)).map(Json.parse).map(_.as[SAPayment])

    println("\n\n\n payments" + payments)
    println("\n\n\n answerDetails " + answerDetails)
    println("\n\n\n questionDataCaches " + questionDataCaches)
    val value = Json.parse(answerDetails.answer.toString)
    println("\n\n\n answerDetails value " + value)
  //  val userAnswer: SAPayment = value.asInstanceOf[SAPayment]
    val userAnswer: SAPayment = Json.fromJson[SAPayment](value).get
    println("\n\n\n answerDetails value1 " + userAnswer)
    println("\n\n\n userAnswer => " + userAnswer)
    payments.exists(payment =>
          (payment.paymentDate, userAnswer.paymentDate) match {
            case (Some(paymentDate), Some(answerDate))
              if payment.amount == userAnswer.amount => insidePaymentToleranceWindow(answerDate, paymentDate)
            case _ => false
          }
        ) match {
          case true => Correct
          case _ => Incorrect
        }
  }
  private def insidePaymentToleranceWindow(dateEntered: LocalDate, expectedDate: LocalDate): Boolean = {
    val diff = Days.daysBetween(dateEntered, expectedDate).getDays
    diff >= (0 - appConfig.saPaymentTolerancePastDays) && diff <= appConfig.saPaymentToleranceFutureDays
  }
}

