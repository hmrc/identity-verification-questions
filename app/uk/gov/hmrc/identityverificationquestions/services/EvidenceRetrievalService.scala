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

package uk.gov.hmrc.identityverificationquestions.services

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Service
import uk.gov.hmrc.identityverificationquestions.sources.empRef.EmpRefService
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcService
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAService
import uk.gov.hmrc.identityverificationquestions.sources.vat.VatReturnsService

import java.time.{LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository,
                                         appConfig: AppConfig,
                                         p60Service: P60Service,
                                         saService: SAService,
                                         payslipService: PayslipService,
                                         ntcService: NtcService,
                                         empRefService: EmpRefService,
                                         vatReturnService:VatReturnsService)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier): Future[QuestionResponse] = {

    val services: Seq[QuestionService] =
      if (appConfig.ntcIsEnabled) Seq(p60Service, saService, payslipService, empRefService, ntcService, vatReturnService)
      else Seq(p60Service, saService, payslipService, empRefService, vatReturnService)

    for {
      questionWithAnswers <- Future.sequence(services.map(_.questions(selection))).map(_.flatten)
      corrId = CorrelationId()
      _ <- {
        mongoRepo.store(QuestionDataCache(
          correlationId = corrId,
          selection = selection,
          questions = questionWithAnswers,
          expiryDate = setExpiryDate))
      }

    } yield toResponse(corrId, questionWithAnswers)
  }

  def setExpiryDate: LocalDateTime = {
    LocalDateTime.now(ZoneOffset.UTC).plus(appConfig.questionRecordTTL)
  }

  private def toResponse(correlationId: CorrelationId, qs: Seq[QuestionWithAnswers]): QuestionResponse = {
    QuestionResponse(correlationId, qs.map(q => Question(q.questionKey, q.info)))
  }

}
