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


sealed trait TaxCreditRecord

case class TaxCreditBankAccount(accountNumber: Option[String], modifiedBacsAccountNumber: Option[String]) extends TaxCreditRecord

sealed abstract class TaxCreditId
case object CTC extends TaxCreditId
case object WTC extends TaxCreditId
case object Sum extends TaxCreditId

case class TaxCreditPayment(date: LocalDate, amount: BigDecimal, taxCreditId: TaxCreditId) extends TaxCreditRecord

object TaxCreditPayment {
  def apply(date: LocalDate, amount: BigDecimal, taxCreditId: TaxCreditId): TaxCreditPayment =
    new TaxCreditPayment(date, amount.setScale(2), taxCreditId)
}

case class TaxCreditClaim(accounts: Seq[TaxCreditBankAccount], payments: Seq[TaxCreditPayment]) extends TaxCreditRecord {
  // API returns payments to the applicant as negative values
  val receivedPayments: Seq[TaxCreditPayment] = payments.collect {
    case TaxCreditPayment(date, amount, id) if amount < 0 => TaxCreditPayment(date, amount * -1, id)
  }
}
