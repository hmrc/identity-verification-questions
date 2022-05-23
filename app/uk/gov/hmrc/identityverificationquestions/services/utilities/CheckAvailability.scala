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

package uk.gov.hmrc.identityverificationquestions.services.utilities

import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName}

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
