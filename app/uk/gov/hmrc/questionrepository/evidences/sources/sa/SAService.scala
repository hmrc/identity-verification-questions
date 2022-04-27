/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.Inject
import play.api.mvc.Request
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors
import uk.gov.hmrc.questionrepository.evidences.sources.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{Question, Selection, selfAssessmentService}
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.services.QuestionService

import scala.concurrent.{ExecutionContext, Future}

class SAService @Inject() (
    val appConfig: AppConfig,
    val saPensionService: SAPensionService,
    val saPaymentService: SAPaymentService,
    val eventDispatcher: EventDispatcher,
    override implicit val auditService: AuditService
                          ) extends QuestionService {
  val serviceName = selfAssessmentService
  override def questions(selection: Selection)
                        (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Seq[Question]] = {
    val pensionQuestionsFuture = saPensionService.questions(selection)
    val paymentQuestionsFuture = saPaymentService.questions(selection)

    for {
      paymentQuestion <- paymentQuestionsFuture
      pensionQuestion <- pensionQuestionsFuture
    } yield if (paymentQuestion.nonEmpty) paymentQuestion else pensionQuestion
  }

  override type Record = SelfAssessmentReturn

//  override def connector: QuestionConnector[SelfAssessmentReturn] = ???
//
//  override def questionHandlers: Seq[QuestionHandler[SelfAssessmentReturn]] =
//    saPensionService.questionHandlers ++ saPaymentService.questionHandlers

  override protected def circuitBreakerConfig: CircuitBreakerConfig = ???

  override def connector: connectors.QuestionConnector[SelfAssessmentReturn] = ???

  override def isAvailable(selection: Selection): Boolean = ???

  override def evidenceTransformer(records: Seq[SelfAssessmentReturn]): Seq[Question] = ???
}
