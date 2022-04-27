/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Passport

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{PassportQuestion, Question, ServiceName, passportService}
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import scala.concurrent.ExecutionContext

@Singleton
class PassportService @Inject()(passportConnector: PassportConnector, val eventDispatcher: EventDispatcher, val auditService: AuditService)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration
{
  override type Record = Boolean

  override def serviceName: ServiceName = passportService

  override def connector: QuestionConnector[Boolean] = passportConnector

  override def evidenceTransformer(records: Seq[Boolean]): Seq[Question] = Seq(Question(questionKey = PassportQuestion, answers = Seq.empty[String]))
}
