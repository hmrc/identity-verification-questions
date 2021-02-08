/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.repository
import javax.inject.Inject
import play.api.libs.functional.syntax._
import play.api.libs.json.{OFormat, __}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.questionrepository.models.{Origin, QuestionId}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository.Entity

import scala.concurrent.{ExecutionContext, Future}

private class QuestionMongoRepository @Inject()(component: ReactiveMongoComponent)(implicit ec: ExecutionContext)
extends ReactiveRepository[QuestionMongoRepository.Entity,String](
  "journey",
  component.mongoConnector.db,
  QuestionMongoRepository.mongoFormat,
  implicitly){


   def store(origin: Origin, questionId: QuestionId, startJourneyUrl: RedirectUrl): Future[Unit] = {
    insert(
      Entity(
        origin = origin.toString,
        questionId = questionId.toString
      )
    ).map(_ => ())
  }
}

private object QuestionMongoRepository{
  case class Entity(origin: String,questionId: String)

  val mongoFormat:OFormat[Entity] = (
    (__ \ "origin").format[String] and
      (__ \ "questionId").format[String]
  )(Entity.apply, unlift(Entity.unapply))
}
