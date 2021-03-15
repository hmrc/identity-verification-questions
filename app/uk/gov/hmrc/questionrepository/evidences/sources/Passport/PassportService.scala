package uk.gov.hmrc.questionrepository.evidences.sources.Passport

import javax.inject.Inject
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{Question, ServiceName, passportService}
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import scala.concurrent.ExecutionContext

class PassportService @Inject()(passportConnector: PassportConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration
{
  override type Record = String

  override def serviceName: ServiceName = passportService

  override def connector: QuestionConnector[String] = passportConnector

  override def evidenceTransformer(records: Seq[String]): Seq[Question] = ???
}
