/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.js.action

import io.gatling.core.action.Interruptable
import io.gatling.core.session.{ Expression, Session }
import akka.actor.{ Props, ActorRef }
import io.gatling.core.validation.Failure
import org.casperjs.{ CasperJSRequestFactory, CasperJSRequest }
import io.gatling.js.async.AsyncCasperJSActor

/**
 * @author Bob Browning
 */
class CasperJSAction(val requestName: Expression[String], val next: ActorRef, val requestFactory: CasperJSRequestFactory) extends Interruptable {

	val asyncCasperJSActorFactory = AsyncCasperJSActor.newAsyncCasperJSActorFactory(next) _

	/**
	 * Core method executed when the Action received a Session message
	 *
	 * @param session the session of the virtual user
	 * @return Nothing
	 */
	def execute(session: Session) {

		def sendRequest(resolvedRequestName: String, request: CasperJSRequest) = {
			logger.info(s"Sending request '$resolvedRequestName': scenario '${session.scenarioName}', userId #${session.userId}")

			val actor = context.actorOf(Props(asyncCasperJSActorFactory(resolvedRequestName)(session)))

			actor ! request
		}

		val request = requestFactory.build(session)

		val execution = for {
			resolvedRequestName <- requestName(session)
		} yield sendRequest(resolvedRequestName, request)

		execution match {
			case Failure(message) =>
				logger.error(message)
				next ! session
			case _ =>
		}

	}
}