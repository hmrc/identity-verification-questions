/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{Question, SCPEmailQuestion, ServiceName, scpEmailService}
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}
import javax.inject.Inject
import play.api.mvc.Request

import scala.concurrent.ExecutionContext

class SCPEmailService @Inject()(scpEmailConnector: SCPEmailConnector)(implicit request: Request[_], override val appConfig: AppConfig, ec: ExecutionContext) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = Option[String]

  override def serviceName: ServiceName = scpEmailService

  override def connector: QuestionConnector[Option[String]] = scpEmailConnector

  override def evidenceTransformer(records: Seq[Option[String]]): Seq[Question] =
    records.flatten match {
      case emails if emails.nonEmpty => Seq(Question(SCPEmailQuestion, emails, Map.empty[String, String]))
      case _ => Seq.empty[Question]
    }

}
