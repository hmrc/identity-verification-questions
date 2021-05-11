package uk.gov.hmrc.questionrepository.evidences.sources.Dvla

import play.api.Logging
import uk.gov.hmrc.http.{CoreGet, CorePost, HeaderCarrier}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.identifier.Identifier
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, CorrelationId, Origin, QuestionResult, ServiceName, dvlaService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DvlaAnswerConnector @Inject()(val http: CoreGet with CorePost)(implicit appConfig: AppConfig, ec: ExecutionContext) extends AnswerConnector[QuestionResult]
  with Logging{

  def serviceName: ServiceName = dvlaService

  override def verifyAnswer(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[QuestionResult] = ???

}
