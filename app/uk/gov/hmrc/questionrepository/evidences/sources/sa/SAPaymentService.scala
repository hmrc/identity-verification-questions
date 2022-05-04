/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.Inject
import org.joda.time.{Days, LocalDate}
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.circuitbreaker.{CircuitBreakerConfig, UnhealthyServiceException}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.evidences.sources.QuestionServiceMeoMinimumNumberOfQuestions
import uk.gov.hmrc.questionrepository.models.{Question, Selection, SelfAssessment, selfAssessmentService}
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.monitoring.{EventDispatcher, ServiceUnavailableEvent}
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}
import uk.gov.hmrc.questionrepository.models.JsonLocalDateFormats.dFormat

import scala.concurrent.{ExecutionContext, Future}

class SAPaymentService @Inject()(connector: SAPaymentsConnector, val eventDispatcher: EventDispatcher, override implicit val auditService: AuditService)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends QuestionServiceMeoMinimumNumberOfQuestions
  with CheckAvailability
  with CircuitBreakerConfiguration {

  type Record = SAPaymentReturn

  def currentDate: LocalDate = LocalDate.now()

  override def serviceName = selfAssessmentService

  val allowedPaymentTypes = List("PYT", "TFO")

  override def questions(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Question]] = {
    if (isAvailable(selection)) {
      withCircuitBreaker {
        selection.sautr match {
          case Some(saUtr) =>
            for {
              _ <- Future(logger.info(s"VER-858:  Retrieve SA UTR($saUtr)"))
              payments <- getRecordsFromSaUtr(saUtr)
              _ = logger.info(s"VER-858:  Retrieve SA UTR($saUtr) response : ${payments.mkString(",")}")
              questions = evidenceTransformer(payments)
              _ = logger.info(s"VER-858: Retrieve SA UTR($saUtr) questions : ${questions.map(_.questionKey).mkString(",")}")
            } yield questions
          case _ => Future.successful(Seq())
        }
      } recover {
        case u: UnhealthyServiceException =>
          auditService.sendCircuitBreakerEvent(selection, serviceName.toString)
          eventDispatcher.dispatchEvent(ServiceUnavailableEvent(serviceName.toString))
          logger.error(s"Unexpected response from $serviceName", u)
          Seq()
        case _: NotFoundException => Seq()
        case t: Throwable => {
          logger.error(s"Unexpected response from $serviceName", t)
          Seq()
        }
      }
    } else {
      Future.successful(Seq())
    }
  }

  override def evidenceTransformer(records: Seq[SAPaymentReturn]): Seq[Question]=
    records.map { paymentReturn =>
    val paymentWindowStartDate = currentDate.minusYears(appConfig.saPaymentWindowYears)
    val recentPositivePayments = paymentReturn.payments.filter { individualPayment =>
      individualPayment.amount > 0 &&
        individualPayment.paymentDate.exists(_.isAfter(paymentWindowStartDate)) &&
        individualPayment.transactionCode.exists(allowedPaymentTypes.contains(_))
    }
    paymentReturn.copy(payments = recentPositivePayments)
  }.filter(_.payments.nonEmpty).map { paymentReturn =>
    convertPaymentToQuestion(paymentReturn)
  }

  private def convertPaymentToQuestion(paymentReturn: SAPaymentReturn): Question = {
    implicit val writes = Json.writes[SAPayment]

    Question(
      SelfAssessment.SelfAssessedPaymentQuestion,
      paymentReturn.payments.map(Json.toJson(_).toString())
    )
  }

  private def getRecordsFromSaUtr(saUtr: SaUtr)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAPaymentReturn]] = {
    connector.getReturns(saUtr)
  }

  override def connector: QuestionConnector[SAPaymentReturn] = connector

}
