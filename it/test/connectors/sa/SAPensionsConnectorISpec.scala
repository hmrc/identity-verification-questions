/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors.sa

import iUtils.{BaseISpec, WireMockStubs}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYear
import uk.gov.hmrc.identityverificationquestions.sources.sa.{SAPensionsConnector, SARecord, SAReturn}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SAPensionsConnectorISpec extends BaseISpec with WireMockStubs {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)

  private val jsonParse: String =
    s"""
       |[
       |  {
       |    "income": 65535,
       |    "returnList": [
       |      {
       |        "utr": "1234567890",
       |        "addressLine1": "123 Fake Street",
       |        "addressLine2": "Fake Town",
       |        "addressLine3": "",
       |        "addressLine4": "",
       |        "postalCode": "SW1A 1AA",
       |        "addressTypeIndicator": "B",
       |        "baseAddressEffectivetDate": "2020-01-01",
       |        "caseStartDate": "2020-01-01",
       |        "receivedDate": "2020-01-01",
       |        "sourceStartDate": "2020-01-01",
       |        "businessDescription": "Shenanigans",
       |        "codedInLaterYear": 1,
       |        "dividendTaxCredits": 2,
       |        "foreignTaxOnEstates": 3,
       |        "giftExtender": 4,
       |        "giftInvestmentsAndPropertyToCharity": 5,
       |        "grossAnnuityPayments": 6,
       |        "incomeFromAllEmployments": 7,
       |        "incomeFromForeign4Sources": 8,
       |        "incomeFromForeignDividends": 9,
       |        "incomeFromGainsOnLifePolicies": 10,
       |        "incomeFromInterestNDividendsFromUKCompaniesNTrusts": 11,
       |        "incomeFromOther": 12,
       |        "incomeFromPensions": 2020.13,
       |        "incomeFromProperty": 14,
       |        "incomeFromSelfAssessment": 15,
       |        "incomeFromSharesOptions": 16,
       |        "incomeFromTrust": 17,
       |        "incomeFromUkInterest": 18,
       |        "incomeTaxReliefReduced": 19,
       |        "incomeTotalWhereTaxIsDue": 20,
       |        "paymentsRetirementAnnuityContract": 21,
       |        "pprExtender": 22,
       |        "profitFromPartnerships": 23,
       |        "profitFromSelfEmployment": 24,
       |        "taxCreditsForeignDividends": 25,
       |        "taxSolvencyStatus": "S",
       |        "totalCapitalGainsDue": 26,
       |        "totalCapitalGainsTaxDue": 27,
       |        "totalIncomeTaxAndCapitalGainsAndNic4Contributions": 28,
       |        "totalIncomeTaxAndNic4Contributions": 29,
       |        "totalTaxAndNic4ContributionsDue": 30,
       |        "totalTaxPaid": 31,
       |        "telephoneNumber": "01234567890"
       |      }
       |    ],
       |    "taxYear": "2020"
       |  },
       |  {
       |    "income": 65535,
       |    "returnList": [
       |      {
       |        "utr": "1234567890",
       |        "addressLine1": "123 Fake Street",
       |        "addressLine2": "Fake Town",
       |        "addressLine3": "",
       |        "addressLine4": "",
       |        "postalCode": "SW1A 1AA",
       |        "addressTypeIndicator": "B",
       |        "baseAddressEffectivetDate": "2020-01-01",
       |        "caseStartDate": "2020-01-01",
       |        "receivedDate": "2020-01-01",
       |        "sourceStartDate": "2020-01-01",
       |        "businessDescription": "Shenanigans",
       |        "codedInLaterYear": 1,
       |        "dividendTaxCredits": 2,
       |        "foreignTaxOnEstates": 3,
       |        "giftExtender": 4,
       |        "giftInvestmentsAndPropertyToCharity": 5,
       |        "grossAnnuityPayments": 6,
       |        "incomeFromAllEmployments": 7,
       |        "incomeFromForeign4Sources": 8,
       |        "incomeFromForeignDividends": 9,
       |        "incomeFromGainsOnLifePolicies": 10,
       |        "incomeFromInterestNDividendsFromUKCompaniesNTrusts": 11,
       |        "incomeFromOther": 12,
       |        "incomeFromPensions": 2019.13,
       |        "incomeFromProperty": 14,
       |        "incomeFromSelfAssessment": 15,
       |        "incomeFromSharesOptions": 16,
       |        "incomeFromTrust": 17,
       |        "incomeFromUkInterest": 18,
       |        "incomeTaxReliefReduced": 19,
       |        "incomeTotalWhereTaxIsDue": 20,
       |        "paymentsRetirementAnnuityContract": 21,
       |        "pprExtender": 22,
       |        "profitFromPartnerships": 23,
       |        "profitFromSelfEmployment": 24,
       |        "taxCreditsForeignDividends": 25,
       |        "taxSolvencyStatus": "S",
       |        "totalCapitalGainsDue": 26,
       |        "totalCapitalGainsTaxDue": 27,
       |        "totalIncomeTaxAndCapitalGainsAndNic4Contributions": 28,
       |        "totalIncomeTaxAndNic4Contributions": 29,
       |        "totalTaxAndNic4ContributionsDue": 30,
       |        "totalTaxPaid": 31,
       |        "telephoneNumber": "01234567890"
       |      }
       |    ],
       |    "taxYear": "2019"
       |  }
       |]
       |""".stripMargin
  private val responseBody: String = Json.parse(jsonParse).toString()

  "get sa returns" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    lazy val connector : SAPensionsConnector = app.injector.instanceOf[SAPensionsConnector]

    val ninoIdentifier: Nino = Nino("AA000003D")
    val startYear = 2019
    val endYear = 2019
    val url = s"/individuals/nino/$ninoIdentifier/self-assessment/income\\?startYear=${startYear.toString}&endYear=${endYear.toString}"

    "successfully obtain a record for valid NINO" in {
      stubGetWithResponseBody(url, OK, responseBody)
      val result: Seq[SAReturn] = await(connector.getReturns(ninoIdentifier, startYear, endYear))

      result shouldBe List(
        SAReturn(TaxYear(2020), List(SARecord(BigDecimal(15), BigDecimal(2020.13)))),
        SAReturn(TaxYear(2019), List(SARecord(BigDecimal(15), BigDecimal(2019.13))))
      )
    }

    "return UpstreamErrorResponse and empty Seq() when P60 data not found" in {
      stubGetWithResponseBody(url, NOT_FOUND, responseBody)
      val result: Seq[SAReturn] = await(connector.getReturns(ninoIdentifier, startYear, endYear))
      result shouldBe Seq.empty
    }
  }

}
