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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import org.joda.time.LocalDate
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class SAPaymentSpec extends AnyWordSpec with Matchers {
  "SA Payment" should {
    "deserialise a single sample response" in new Setup {
      val actual = sampleSingleResponseJson.as[SAPayment]

      actual.amount shouldBe 1515
      actual.paymentDate shouldBe Some(LocalDate.parse("2011-07-31"))
      actual.transactionCode shouldBe Some("BCC")
    }

    "deserialise an array of returns" in new Setup {
      val actual = sampleArrayOfResponsesJson.as[Seq[SAPayment]]

      actual.size shouldBe 3
      actual(0).amount shouldBe 3800
      actual(0).transactionCode shouldBe Some("PYT")
      actual(1).amount shouldBe 1515
      actual(1).transactionCode shouldBe Some("BCC")
      actual(2).amount shouldBe 0
      actual(2).transactionCode shouldBe Some("BCC")
    }
  }

  trait Setup {
    val sampleSingleResponsesString: String =
      """
        |{
        |  "createdDate": "2011-07-31",
        |  "transactionCode": "BCC",
        |  "amount": {
        |    "amount": 1515,
        |    "currency": "GBP"
        |  },
        |  "transactionId": {
        |    "tieBreaker": 1234,
        |    "sequenceNumber": null,
        |    "creationDate": "2011-07-31"
        |  },
        |  "taxYearEnd": "2012-04-05"
        |}
        |""".stripMargin

    val sampleSingleResponseJson = Json.parse(sampleSingleResponsesString)

    val sampleArrayOfResponsesString: String =
      """
        |{
        |  "paymentsList": [
        |    {
        |      "createdDate": "2011-01-31",
        |      "transactionCode": "PYT",
        |      "amount": {
        |        "amount": 3800,
        |        "currency": "GBP"
        |      },
        |      "transactionId": {
        |        "tieBreaker": 9534,
        |        "sequenceNumber": null,
        |        "creationDate": "2011-01-31"
        |      },
        |      "taxYearEnd": "null"
        |    },
        |    {
        |      "createdDate": "2011-07-31",
        |      "transactionCode": "BCC",
        |      "amount": {
        |        "amount": 1515,
        |        "currency": "GBP"
        |      },
        |      "transactionId": {
        |        "tieBreaker": 1234,
        |        "sequenceNumber": null,
        |        "creationDate": "2011-07-31"
        |      },
        |      "taxYearEnd": "2012-04-05"
        |    },
        |    {
        |      "createdDate": "2011-05-31",
        |      "transactionCode": "BCC",
        |      "amount": {
        |        "amount": 0,
        |        "currency": "GBP"
        |      },
        |      "transactionId": {
        |        "tieBreaker": 1234,
        |        "sequenceNumber": null,
        |        "creationDate": "2011-05-31"
        |      },
        |      "taxYearEnd": "2012-04-05"
        |    }
        |  ]
        |}
        |""".stripMargin

    val sampleArrayOfResponsesJson = Json.parse(sampleArrayOfResponsesString)
  }
}
