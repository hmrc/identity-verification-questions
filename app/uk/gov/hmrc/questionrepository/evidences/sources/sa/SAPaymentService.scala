/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.Inject
import jdk.tools.jlink.internal.Utils.isDisabled
import org.joda.time.{Days, LocalDate}
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.circuitbreaker.{CircuitBreakerConfig, UnhealthyServiceException}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.{HodCircuitBreakerConfig, QuestionServiceMeoMinimumNumberOfQuestions}
import uk.gov.hmrc.questionrepository.models.identifier.UtrType
import uk.gov.hmrc.questionrepository.models.{Question, Selection}
import uk.gov.hmrc.questionrepository.monitoring.{EventDispatcher, ServiceUnavailableEvent}
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe.Match

class SAPaymentService @Inject()(connector: SAPaymentsConnector, val eventDispatcher: EventDispatcher, override implicit val auditService: AuditService)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends QuestionServiceMeoMinimumNumberOfQuestions
  with AnswerUrl
  with CircuitBreakerConfig {

  type Record = SelfAssessmentReturn

  def currentDate: LocalDate = LocalDate.now()

  override def serviceName: String = "SelfAssessmentPaymentService"

  override def questionHandlers: Seq[QuestionHandler[SelfAssessmentReturn]] = Seq()

  val allowedPaymentTypes = List("PYT", "TFO")

  override def questions(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Question]] = {
    if (isDisabled(journey.serviceContract.origin)) {
      Future.successful(Seq())
    } else {
      withCircuitBreaker {
        val saUtrOption = selection.identifiers.find(p => p.identifierType == UtrType)
        saUtrOption match {
          case Some(saUtr) =>
            for {
              _ <- Future(logger.info(s"VER-858:  Retrieve SA UTR($saUtr)"))
              payments <- getRecordsFromSaUtr(saUtr)
              _ = logger.info(s"VER-858:  Retrieve SA UTR($saUtr) response : ${payments.mkString(",")}")
              questions = convertPaymentsToQuestions(payments, journey)
              _ = logger.info(s"VER-858: ${journey.journeyId} Retrieve SA UTR($saUtr) questions : ${questions.map(_.questionKey).mkString(",")}")
            } yield questions
          case _ => Future.successful(Seq())
        }
      } recover {
        case u: UnhealthyServiceException =>
          auditService.sendCircuitBreakerEvent(journey, serviceName)
          eventDispatcher.dispatchEvent(ServiceUnavailableEvent(serviceName))
          logger.error(s"Unexpected response from $serviceName", u)
          Seq()
        case _: NotFoundException => Seq()
        case t: Throwable => {
          logger.error(s"Unexpected response from $serviceName", t)
          Seq()
        }
      }
    }
  }

  private def convertPaymentsToQuestions(paymentReturns: Seq[SAPaymentReturn], journey: Journey)(
    implicit request: Request[_]
  ): Seq[Question] = paymentReturns.map { paymentReturn =>
    val paymentWindowStartDate = currentDate.minusYears(appConfig.saPaymentWindowYears)
    val recentPositivePayments = paymentReturn.payments.filter { individualPayment =>
      individualPayment.amount > 0 &&
        individualPayment.paymentDate.exists(_.isAfter(paymentWindowStartDate)) &&
        individualPayment.transactionCode.exists(allowedPaymentTypes.contains(_))
    }
    paymentReturn.copy(payments = recentPositivePayments)
  }.filter(_.payments.nonEmpty).map { paymentReturn =>
    convertPaymentToQuestion(paymentReturn, journey)
  }

  private def convertPaymentToQuestion(paymentReturn: SAPaymentReturn, journey: Journey)(implicit request: Request[_]): Question = {
    implicit val writes = Json.writes[SAPayment]

    Question(
      SelfAssessmentEvidence.SelfAssessedPaymentQuestion,
      paymentReturn.payments.map(Json.toJson(_).toString())
    )
  }

  private def getRecordsFromSaUtr(saUtr: SaUtr)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAPaymentReturn]] = {
    connector.getReturns(saUtr)
  }

  def validateAnswer(question: Question, answer: SAPayment): Future[AnswerCorrectness] = Future {
    implicit val reads = Json.reads[SAPayment]

    val answers: Seq[SAPayment] = question.answers.map(Json.parse(_)).map(_.as[SAPayment])

    answers.exists(payment =>
      (payment.paymentDate, answer.paymentDate) match {
        case (Some(paymentDate), Some(answerDate))
          if payment.amount == answer.amount => insidePaymentToleranceWindow(answerDate, paymentDate)
        case _ => false
      }
    ) match {
      case true => Match
      case _ => NoMatch(answers.map(answerAsText))
    }
  }

  private def answerAsText(payment: SAPayment): String = {
    s"${payment.amount} on ${payment.paymentDate.map(_.toString("dd/MMM/yyyy")).getOrElse("Unspecified Date")}"
  }

  private def insidePaymentToleranceWindow(dateEntered: LocalDate, expectedDate: LocalDate): Boolean = {
    val diff = Days.daysBetween(dateEntered, expectedDate).getDays
    diff >= (0 - appConfig.saPaymentTolerancePastDays) && diff <= appConfig.saPaymentToleranceFutureDays
  }
}
