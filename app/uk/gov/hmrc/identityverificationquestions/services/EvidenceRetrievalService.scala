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
import uk.gov.hmrc.identityverificationquestions.sources.P45.P45Service
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Service
import uk.gov.hmrc.identityverificationquestions.sources.empRef.EmpRefService
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcService
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAService

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository,
                                         appConfig: AppConfig,
                                         p60Service: P60Service,
                                         p45Service: P45Service,
                                         saService: SAService,
                                         payslipService: PayslipService,
                                         ntcService: NtcService,
                                         empRefService: EmpRefService)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection, userAgent: String)(implicit request: Request[_], hc: HeaderCarrier): Future[QuestionResponse] = {

    val services: Seq[QuestionService] =
      if (appConfig.ntcIsEnabled) Seq(p60Service, p45Service, saService, payslipService, empRefService, ntcService)
      else Seq(p60Service, p45Service, saService, payslipService, empRefService)

    val corrId: CorrelationId = CorrelationId()

    for {
      questionWithAnswers <- Future.sequence(services.filter(_.isUserAllowed(userAgent)).map(_.questions(selection, corrId))).map(_.flatten)
      _ <- {
        mongoRepo.store(QuestionDataCache(
          correlationId = corrId,
          selection = selection,
          questions = questionWithAnswers,
          expiryDate = setExpiryDate))
      }

    } yield toResponse(corrId, questionWithAnswers)
  }

  def setExpiryDate: Instant = {
    Instant.now().plus(appConfig.questionRecordTTL)
  }

  private def toResponse(correlationId: CorrelationId, qs: Seq[QuestionWithAnswers]): QuestionResponse = {
    QuestionResponse(correlationId, qs.map(q => Question(q.questionKey, q.info)))
  }

}
