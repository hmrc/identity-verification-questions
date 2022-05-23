/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}

class SARecordSpec extends AnyWordSpec with Matchers {
  "SA Record" should {
    "deserialise a sample response" in new Setup {
      val actual = sampleResponseJson.as[SARecord]

      actual.selfAssessmentIncome shouldBe 25500.64
      actual.incomeFromPensions shouldBe 111.11
    }

    "deserialise a sample respone with a missing incomeFromPensions" in new Setup {
      val jsonWithoutIncomeFromPensions = sampleResponseJson - "incomeFromPensions"
      val actual = jsonWithoutIncomeFromPensions.as[SARecord]

      actual.incomeFromPensions shouldBe 0
    }
  }

  trait Setup {
    val sampleResponseString: String =
      """
        |{
        |  "utr": "0987654321",
        |  "caseStartDate": "2014-07-15",
        |  "receivedDate": "2015-01-01",
        |  "sourceStartDate": "2015-01-01",
        |  "taxSolvencyStatus": "S",
        |  "codedInLaterYear": 0,
        |  "dividendTaxCredits": 0,
        |  "incomeFromSelfAssessment": 25500.64,
        |  "incomeFromTrust": 0,
        |  "incomeFromAllEmployments": 15000.00,
        |  "profitFromSelfEmployment": 10500.64,
        |  "profitFromPartnerships": 0,
        |  "incomeFromProperty": 111.14,
        |  "incomeFromForeign4Sources": 0,
        |  "incomeFromForeignDividends": 0,
        |  "incomeFromUkInterest": 0,
        |  "incomeFromInterestNDividendsFromUKCompaniesNTrusts": 0,
        |  "incomeFromPensions": 111.11,
        |  "incomeFromOther": 0,
        |  "incomeFromGainsOnLifePolicies": 0,
        |  "incomeFromSharesOptions": 0,
        |  "incomeTotalWhereTaxIsDue": 0,
        |  "taxCreditsForeignDividends": 0,
        |  "totalCapitalGainsDue": 0,
        |  "totalCapitalGainsTaxDue": 0,
        |  "totalIncomeTaxAndCapitalGainsAndNic4Contributions": 0,
        |  "totalIncomeTaxAndNic4Contributions": 0,
        |  "totalTaxAndNic4ContributionsDue": 0,
        |  "totalTaxPaid": 2550.06,
        |  "incomeTaxReliefReduced": 0,
        |  "paymentsRetirementAnnuityContract": 0,
        |  "giftInvestmentsAndPropertyToCharity": 0,
        |  "grossAnnuityPayments": 0,
        |  "foreignTaxOnEstates": 0,
        |  "pprExtender": 0,
        |  "giftExtender": 0,
        |  "businessDescription": "Sweet Shop",
        |  "addressLine1": "12 Main Street",
        |  "addressLine2": "City Park",
        |  "addressLine3": "Shrewsbury",
        |  "addressLine4": "Shropshire",
        |  "postalCode": "SY1 4GB",
        |  "telephoneNumber": "123456789",
        |  "baseAddressEffectivetDate": "1920-02-29",
        |  "addressTypeIndicator": "B"
        |}
        |""".stripMargin

    val sampleResponseJson : JsObject = Json.parse(sampleResponseString).as[JsObject]
  }
}
