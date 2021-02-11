/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Payment.Payment
import uk.gov.hmrc.questionrepository.models.Question
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.Inject

class P60Service @Inject()(connector: P60Connector)(implicit override val appConfig: AppConfig) extends QuestionService with CheckAvailability with CircuitBreakerConfiguration {
  override type Record = Payment

  override def serviceName: String = "p60Service"

  override def connector: QuestionConnector[Payment] = connector

  override def evidenceTransformer(records: Seq[Payment]): Seq[Question] = ???
}
