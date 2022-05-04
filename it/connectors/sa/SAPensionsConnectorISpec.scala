package connectors.sa

import iUtils.BaseISpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.sa.{SAPensionsConnector, SARecord, SAReturn}
import uk.gov.hmrc.questionrepository.services.utilities.TaxYear

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SAPensionsConnectorISpec extends BaseISpec {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().build()

  "get sa returns" should {
    "successfully obtain a return" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector : SAPensionsConnector = fakeApplication.injector.instanceOf[SAPensionsConnector]

      val result: Seq[SAReturn] = await(connector.getReturns(Nino("AA000003D"), 2019, 2019))

      result shouldBe List(SAReturn(TaxYear(2019), List(SARecord(BigDecimal(15), BigDecimal(2019.13)))))
    }
  }

}
