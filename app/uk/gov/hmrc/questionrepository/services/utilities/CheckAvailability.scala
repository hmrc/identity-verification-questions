/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{Identifier, Origin}

import java.time.LocalDateTime

trait CheckAvailability {

   implicit val appConfig: AppConfig

   def serviceName: String

   private lazy val serviceStatus = appConfig.serviceStatus(serviceName)

   private def hasRequiredIdentifiers(identifiers: Seq[Identifier]): Boolean =
      serviceStatus.requiredIdentifiers.forall(identifiers.map(_.identifierType).contains)

   private def isDisabledByTime(dateToCheck: LocalDateTime = LocalDateTime.now()): Boolean =
      serviceStatus.outage match {
         case Some(outage) => outage.startDate.isBefore(dateToCheck) && outage.endDate.isAfter(dateToCheck)
         case _ => false
      }

   private def isDisabledByOrigin(origin: Origin): Boolean =
      serviceStatus.disabledOrigins.contains(origin.value)

   private def isEnabledByOrigin(origin: Origin): Boolean =
      serviceStatus.enabledOrigins.contains(origin.value)

   def isAvailable(origin: Origin, identifiers: Seq[Identifier]): Boolean =
      if (!serviceStatus.enabledOrigins.isEmpty)
         isEnabledByOrigin(origin) && hasRequiredIdentifiers(identifiers) && !isDisabledByTime()
      else
         !isDisabledByOrigin(origin) && hasRequiredIdentifiers(identifiers) && !isDisabledByTime()

}
