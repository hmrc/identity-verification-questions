package uk.gov.hmrc.questionrepository.evidences.sources.Dvla

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, DVLAQuestion, QuestionKey, QuestionResult, ServiceName, dvlaService}
import uk.gov.hmrc.questionrepository.services.AnswerService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DvlaAnswerService @Inject()(dvlaAnswerConnector: DvlaAnswerConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends AnswerService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def connector: AnswerConnector[QuestionResult] = dvlaAnswerConnector

  override def supportedQuestions: Seq[QuestionKey] = Seq(DVLAQuestion)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

  override def serviceName: ServiceName = dvlaService
}
