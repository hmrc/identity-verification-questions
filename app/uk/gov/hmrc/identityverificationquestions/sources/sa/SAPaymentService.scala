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

import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.circuitbreaker.UnhealthyServiceException
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.monitoring.{EventDispatcher, ServiceUnavailableEvent}
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SAPaymentService @Inject()(connector: SAPaymentsConnector,
                                 val eventDispatcher: EventDispatcher,
                                 val auditService: AuditService,
                                 val appConfig: AppConfig,
                                 val metricsService: MetricsService) extends QuestionServiceMeoMinimumNumberOfQuestions
  with CheckAvailability
  with CircuitBreakerConfiguration {

  type Record = SAPaymentReturn

  def currentDate: LocalDate = LocalDate.now()

  override def serviceName: ServiceName = selfAssessmentService

  val allowedPaymentTypes = List("PYT", "TFO")

  override def questions(selection: Selection, corrId: CorrelationId)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[QuestionWithAnswers]] = {
    val origin = request.headers.get("user-agent").getOrElse("unknown origin")
    if (isAvailableForRequestedSelection(selection)) {
      withCircuitBreaker {
        selection.sautr match {
          case Some(saUtr) =>
            for {
              _ <- Future(logger.info(s"VER-858:  Retrieve SA UTR($saUtr)"))
              payments <- getRecordsFromSaUtr(saUtr)
              _ = logger.info(s"VER-858:  Retrieve SA UTR($saUtr) response : ${payments.mkString(",")}")
              questions = evidenceTransformer(payments, corrId)
              _ = logger.info(s"VER-858: Retrieve SA UTR($saUtr) questions : ${questions.map(_.questionKey).mkString(",")}")
            } yield questions
          case _ => Future.successful(Seq())
        }
      } recover {
        case u: UnhealthyServiceException =>
          auditService.sendCircuitBreakerEvent(selection, serviceName.toString)
          eventDispatcher.dispatchEvent(ServiceUnavailableEvent(serviceName.toString))
          logger.error(s"$serviceName threw UnhealthyServiceException, origin: $origin")
          Seq()
        case _: NotFoundException => Seq()
        case t: Throwable =>
          logger.error(s"$serviceName threw Exception, origin: $origin; detail: $t")
          Seq()
      }
    } else {
      Future.successful(Seq())
    }
  }

  override def evidenceTransformer(records: Seq[SAPaymentReturn], corrId: CorrelationId): Seq[QuestionWithAnswers]=
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

  private def convertPaymentToQuestion(paymentReturn: SAPaymentReturn): QuestionWithAnswers = {

    QuestionWithAnswers(
      SelfAssessment.SelfAssessedPaymentQuestion,
      paymentReturn.payments.map(Json.toJson(_).toString())
    )
  }

  private def getRecordsFromSaUtr(saUtr: SaUtr)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAPaymentReturn]] = {
    connector.getReturns(saUtr)
  }

  override def connector: QuestionConnector[SAPaymentReturn] = connector

}
