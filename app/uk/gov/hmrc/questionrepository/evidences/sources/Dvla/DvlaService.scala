/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Dvla

import javax.inject.Inject
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{DVLAQuestion, Question, ServiceName, dvlaService}
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import scala.concurrent.ExecutionContext

class DvlaService @Inject()(dvlaConnector: DvlaConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends QuestionService
  with CheckAvailability with CircuitBreakerConfiguration {

  override type Record = Boolean

  override def serviceName: ServiceName = dvlaService

  override def connector: QuestionConnector[Boolean] = dvlaConnector

  override def evidenceTransformer(records: Seq[Boolean]): Seq[Question] = Seq(Question(questionKey = DVLAQuestion, answers = Seq.empty[String]))
}
