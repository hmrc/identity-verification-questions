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

package uk.gov.hmrc.identityverificationquestions.sources.ntc

import org.joda.time.LocalDate
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.taxcredit.{TaxCreditBankAccount, TaxCreditClaim, TaxCreditPayment, TaxCreditRecord}
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.QuestionService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.{Inject, Singleton}

@Singleton
class NtcService @Inject()(ntcConnector: NtcConnector, val eventDispatcher: EventDispatcher, val auditService: AuditService)(implicit override val appConfig: AppConfig)
  extends QuestionService with CheckAvailability with CircuitBreakerConfiguration {

  lazy val paymentMonths: Int = appConfig.ntcPaymentMonths

  override type Record = TaxCreditRecord

  override def serviceName: ServiceName = taxCreditService

  override def connector: QuestionConnector[TaxCreditRecord] = ntcConnector

  def last4digits(str: String): String = str takeRight 4

  def meetsDate(date: LocalDate): Boolean =  LocalDate.now.minusMonths(paymentMonths).isBefore(date)

  def positiveAmount(amout : BigDecimal): Boolean = amout > 0

  override def evidenceTransformer(records: Seq[TaxCreditRecord]): Seq[QuestionWithAnswers] = {
    val taxCreditQuestions: Seq[(QuestionKey, String)] = records.flatMap {
      case TaxCreditClaim(accounts, payments) if payments.filter(pay => meetsDate(pay.date)).filter(pay => positiveAmount(pay.amount)).seq.nonEmpty =>
        accounts flatMap {
          case TaxCreditBankAccount(accountNumber, modifiedBacsAccountNumber) =>
            (accountNumber.collect { case n if n.length >= 4 => n }.toSeq ++
              modifiedBacsAccountNumber.collect { case n if n.length >= 4 => n }.toSeq)
              .map { digits =>
                last4digits(digits)
                (TaxCredits.BankAccount, last4digits(digits))
              }
        }

      case TaxCreditPayment(date, amount, _) if meetsDate(date) => Seq((TaxCredits.Amount, amount.toString))

      case _ => Seq.empty[(QuestionKey, String)]

    }.distinct

    taxCreditQuestions.groupBy(_._1).map{ question =>
      QuestionWithAnswers(question._1, question._2.map{ d =>d._2})
    }.toSeq

  }
}
