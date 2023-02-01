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

package uk.gov.hmrc.identityverificationquestions.models.taxcredit

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.http.controllers.JsPathEnrichment._
import uk.gov.hmrc.identityverificationquestions.models.JsonLocalDateFormats.dFormat


trait NtcJsonFormats {

  def idFromString(value: String) = value match {
    case "WTC" => WTC
    case "CTC" => CTC
  }

  implicit val taxCreditPayment: Reads[Option[TaxCreditPayment]] = (
    (__ \ "subjectDate").readNullable[LocalDate] and
    (__ \ "amount").readNullable[BigDecimal] and
    (__ \ "taxCreditId").readNullable[String]
  )((date, amount, id) => (date, amount, id) match {
    // we can only deal with a payment properly if all three properties are populated
    case (Some(date), Some(amount), Some(taxCreditId@("WTC"|"CTC"))) => Some(TaxCreditPayment(date, amount, idFromString(taxCreditId)))
    case _ => None
  }
  )
  
  implicit val accountReads: Reads[TaxCreditBankAccount] = Json.reads[TaxCreditBankAccount].map { acc =>
  
    val maybeAccNumber= acc.accountNumber.flatMap {
      num => if (num.trim.isEmpty) None else Some(num.trim)
    }
    val maybeBacsAccNumber = acc.modifiedBacsAccountNumber.flatMap {
      num => if (num.trim.isEmpty) None else Some(num.trim)
    }

    TaxCreditBankAccount(maybeAccNumber, maybeBacsAccNumber)
  }
  
  implicit val taxCreditClaimReads: Reads[TaxCreditClaim] = (
    (__ \ "applicant1" \ "bankOrBuildingSociety").tolerantReadNullable[TaxCreditBankAccount] and
    (__ \ "applicant2" \ "bankOrBuildingSociety").tolerantReadNullable[TaxCreditBankAccount] and
    (__ \ "previousPayment").readNullable[Seq[Option[TaxCreditPayment]]]
  )((account1, account2, payments) => {
    val accounts = account1.toSeq ++ account2.toSeq
    val filteredPayments = payments.map(p => p.collect{case Some(payment) => payment}).getOrElse(Seq())
    TaxCreditClaim(accounts, filteredPayments)
  })
  
}
