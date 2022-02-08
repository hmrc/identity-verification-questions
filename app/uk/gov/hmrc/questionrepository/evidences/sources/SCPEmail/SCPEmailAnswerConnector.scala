/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail

import uk.gov.hmrc.questionrepository.connectors.MongoAnswerConnector
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SCPEmailAnswerConnector @Inject()(questionRepo: QuestionMongoRepository)(implicit ec: ExecutionContext) extends MongoAnswerConnector(questionRepo) {

}
