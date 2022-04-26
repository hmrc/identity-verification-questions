/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, CoreGet}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{AccountInformation, DetailsNotFound, NinoClStoreEntry, Selection, ServiceName, scpEmailService}
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SCPEmailConnector @Inject()(val http: CoreGet)
                                 (implicit val appConfig: AppConfig) extends QuestionConnector[Option[String]] {
  
  lazy val basProxyBaseUrl: String = appConfig.basProxyBaseUrl + "/bas-proxy"
  lazy val identityVerificationBaseUrl: String = appConfig.identityVerificationBaseUrl

  def serviceName: ServiceName =  scpEmailService

  private def findNinoClStoreCredentials(ninoClStoreEntries: List[NinoClStoreEntry]): List[String] = ninoClStoreEntries.map(_.credId).distinct

  private def getAccountInformation(credId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    val url = s"$basProxyBaseUrl/credentials/$credId"
    http.GET[Option[AccountInformation]](url).map(accountInformation => accountInformation.flatMap(_.email))
  }

  private def getEmail(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Option[String]]] = {
    val url = s"$identityVerificationBaseUrl/identity-verification/nino?nino=${nino.value}"
    http.GET[List[NinoClStoreEntry]](url).map{findNinoClStoreCredentials}.flatMap{credIds =>
      credIds.size match{
        case 1 => getAccountInformation(credIds.head).map(Seq(_))
        case _ => throw DetailsNotFound
          //TODO when auditing is made, create audit for when size is > 1
      }
    }
  }

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Option[String]]] = {
    getEmail(selection.nino.getOrElse(throw DetailsNotFound))
  }

}
