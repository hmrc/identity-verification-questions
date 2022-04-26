/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{Selection, ServiceName}

import java.time.LocalDateTime

trait CheckAvailability {

   implicit val appConfig: AppConfig

   def serviceName: ServiceName

   private lazy val serviceStatus = appConfig.serviceStatus(serviceName)

   private def hasRequiredIdentifiers(selection: Selection): Boolean = {
      serviceStatus.requiredIdentifiers.forall {
         case "nino" => selection.nino.isDefined // TODO move to Selection?
         case "utr" => selection.sautr.isDefined
         case "dob" => selection.dob.isDefined
         case _ => false
      }
   }

   private def isDisabledByTime(dateToCheck: LocalDateTime = LocalDateTime.now()): Boolean =
      serviceStatus.outage match {
         case Some(outage) => outage.startDate.isBefore(dateToCheck) && outage.endDate.isAfter(dateToCheck)
         case _ => false
      }

   def isAvailable(selection: Selection): Boolean =
      hasRequiredIdentifiers(selection) && !isDisabledByTime()

}
