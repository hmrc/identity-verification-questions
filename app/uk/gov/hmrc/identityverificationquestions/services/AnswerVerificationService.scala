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

package uk.gov.hmrc.identityverificationquestions.services

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.{AnswerCheck, QuestionKey, QuestionResult}
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60AnswerService
import uk.gov.hmrc.identityverificationquestions.sources.empRef.EmpRefAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipAnswerService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAAnswerService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnswerVerificationService @Inject()(p60AnswerService: P60AnswerService,
                                          saAnswerService: SAAnswerService,
                                          payslipAnswerService: PayslipAnswerService,
                                          ntcAnswerService: NtcAnswerService,
                                          empRefAnswerService: EmpRefAnswerService)(implicit ec: ExecutionContext) {

  val answerServices = Seq(p60AnswerService, saAnswerService, payslipAnswerService, empRefAnswerService, ntcAnswerService)

  private def getQuestionService(questionKey: QuestionKey): AnswerService = {
    answerServices.filter(_.supportedQuestions.contains(questionKey)) match {
      case answerServices if answerServices.isEmpty => throw new RuntimeException(s"Could not find evidence source for questionKey for $questionKey")
      case answerServices if answerServices.size > 1 => throw new RuntimeException(s"Could not find unique evidence source for questionKey for $questionKey")
      case answerServices => answerServices.head
    }
  }

  def checkAnswers(answerToCheck: AnswerCheck)(implicit request: Request[_], hc: HeaderCarrier): Future[Seq[QuestionResult]] = {
    for {
      seqSeqQuestionResult <- Future.sequence(answerToCheck.answers.map { answer =>
        getQuestionService(answer.questionKey).checkAnswers(answerToCheck)
      })
      result = seqSeqQuestionResult.flatten
    } yield result
  }
}
