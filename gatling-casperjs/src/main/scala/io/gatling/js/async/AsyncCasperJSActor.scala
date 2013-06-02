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
package io.gatling.js.async

import akka.actor.ActorRef
import io.gatling.core.action.BaseActor
import io.gatling.core.session.Session
import io.gatling.core.util.TimeHelper.nowMillis

import scala.Some
import org.casperjs.CasperJSRequest
import io.gatling.core.result.message._
import io.gatling.core.result.writer.DataWriter
import io.gatling.js.response._

object AsyncCasperJSActor {
	def newAsyncCasperJSActorFactory(next: ActorRef)(requestName: String) =
		(session: Session) => new AsyncCasperJSActor(session, next, requestName)
}

/**
 * @author Bob Browning
 */
class AsyncCasperJSActor(var session: Session, next: ActorRef, requestName: String) extends BaseActor {

	val responseBuilder = new ResponseBuilder()

	def receive = {
		case request: CasperJSRequest =>
			if (request.execute(responseBuilder.logger) == 0) {
				responseBuilder.updateExecutionEndDate
				ok(session, responseBuilder.build)
			} else {
				ko(session, responseBuilder.build, s"Execute of ${requestName} failed.")
			}
	}

	private def logRequest(session: Session, status: Status, response: Response, errorMessage: Option[String] = None) {

		if (status == KO) {
			logger.warn(s"Request '$requestName' failed : ${errorMessage.getOrElse("")}")
		}

		DataWriter.tell(RequestMessage(session.scenarioName, session.userId, session.groupStack, requestName,
			response.executionStartDate, response.requestSendingEndDate, response.responseReceivingStartDate, response.executionEndDate,
			status, errorMessage))
	}

	/**
	 * This method is used to send a message to the data writer actor and then execute the next action
	 *
	 * @param newSession the new Session
	 */
	private def executeNext(newSession: Session, response: Response) {
		next ! newSession.increaseTimeShift(nowMillis - response.executionEndDate)
		context.stop(self)
	}

	private def ok(session: Session, response: Response) {
		logRequest(session, OK, response, None)
		executeNext(session, response)
	}

	private def ko(session: Session, response: Response, message: String) {
		val failedSession = session.markAsFailed
		logRequest(failedSession, KO, response, Some(message))
		executeNext(failedSession, response)
	}
}
