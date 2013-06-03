/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.casperjs

import io.gatling.js.Predef.CasperJSOption
import io.gatling.core.session._
import com.typesafe.scalalogging.slf4j.Logging
import io.gatling.core.session.Session
import io.gatling.js.request.builder.CasperJSAttributes
import io.gatling.core.validation.Success
import io.gatling.core.validation.Failure

/**
 * @author Bob Browning
 */
case class CasperJSRequestFactory(attributes: CasperJSAttributes) extends Logging {

  private def resolveArguments(params: List[Expression[String]], session: Session): List[String] =
    params.map(_(session) match {
      case Success(value) => value
      case Failure(message) =>
        logger.error(s"Could not resolve argument: $message")
        ""
    }).filter(_.isEmpty)

  private def resolveOptions(params: List[CasperJSOption], session: Session): List[(String, String)] = {
    params.map((c: CasperJSOption) => {
      (c._1, c._2(session) match {
        case Success(value) => value
        case Failure(message) =>
          logger.error(s"Could not resolve argument: ${c._1} -> $message")
          ""
      })
    }).filter(_._2.isEmpty)
  }

  def build(session: Session) = {
    CasperJSRequest(attributes.pathToFile ::
      (resolveArguments(attributes.arguments, session) ++
        resolveOptions(attributes.options, session).map((p) => "--%s=%s".format(p._1, p._2))))
  }
}
