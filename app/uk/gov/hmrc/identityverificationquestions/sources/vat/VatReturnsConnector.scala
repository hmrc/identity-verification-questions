/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.sources.vat

import play.api.Logging
import uk.gov.hmrc.http.{BadRequestException, CoreGet, HeaderCarrier, HttpException, NotFoundException}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName, VatReturnSubmission, vatService}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatReturnsConnector @Inject()(val http: CoreGet,  servicesConfig: ServicesConfig)(implicit val appConfig: AppConfig) extends QuestionConnector[VatReturnSubmission]
  with HodConnectorConfig with Logging {
  lazy val baseUrl: String = servicesConfig.baseUrl("vatService")


  override def getRecords(selection: Selection)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[VatReturnSubmission]] = {


    val desHeaders: HeaderCarrier = headersForDES
    val headers = desHeaders.headers(List("Authorization", "X-Request-Id")) ++ desHeaders.extraHeaders

    def getUserVatReturnDetails(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, appConfig: AppConfig): Future[Seq[VatReturnSubmission]] = {
    val url = s"$baseUrl/vat/returns/vrn/$vrn"

    /** *
     * The period key is 4 characters long, usually the 1st two character represent the year.
     * Example VAT ones are:
     * 20AA January 2020
     * 20AB February 2020
     * 20AC March 2020
     * However it would depend on the type of VAT account and how its obligations are set out.
     * For example an annual filer would have tax codes of
     * 20YA 2020 annual return
     * 21YA 2021 annual return
     * * */

    val periodKey = "22YA"
    val queryParams: Seq[(String, String)] = Seq("period-key" -> periodKey)

      println(s"#####\n###### url is ${url}###\nheaders are ${headers}###\n##")
    http.GET[VatReturnSubmission](url, queryParams, headers = headers).map { response =>
      println("######## response:" + response)
      Seq(response)
    }
      .recover {
        case e: BadRequestException =>
          logger.warn(s"MTD VAT Returns 400, ${e.message}")
          Seq()
        case e: NotFoundException =>
          logger.warn(s"MTD VAT Returns 404, ${e.message}")
          Seq()
        case e: HttpException =>
          logger.error(s"MTD VAT Returns exception: ${e.message}")
          Seq()
      }
  }

    selection.vrn.map { vrn =>
      getUserVatReturnDetails(vrn.vrn)
    }.getOrElse {
      logger.warn(s"$serviceName, No vrn for selection: $selection")
      Future.successful(Seq.empty[VatReturnSubmission])
    }
  }

  override def serviceName: ServiceName = vatService
}
