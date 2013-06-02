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
package io.gatling.js.response

import scala.math.max

import io.gatling.core.util.TimeHelper.nowMillis

import scala.sys.process.ProcessLogger

/**
 * @author Bob Browning
 */

sealed case class Response(out: Seq[String], err: Seq[String],
	executionStartDate: Long, requestSendingEndDate: Long,
	responseReceivingStartDate: Long, executionEndDate: Long)

class ResponseBuilder {

	val out: Seq[String] = Seq.empty
	val err: Seq[String] = Seq.empty

	val _executionStartDate = nowMillis
	var _requestSendingEndDate = 0L
	var _responseReceivingStartDate = 0L
	var _executionEndDate = 0L

	def logger: ProcessLogger = ProcessLogger(
		(message: String) => out ++ message,
		(message: String) => err ++ message)

	def updateRequestSendingEndDate = {
		_requestSendingEndDate = nowMillis
		this
	}

	def updateResponseReceivingStartDate = {
		_responseReceivingStartDate = nowMillis
		this
	}

	def updateExecutionEndDate = {
		_executionEndDate = nowMillis
		this
	}

	def build: Response = {
		// time measurement is imprecise due to multi-core nature
		// ensure request doesn't end before starting
		_requestSendingEndDate = max(_requestSendingEndDate, _executionStartDate)
		// ensure response doesn't start before request ends
		_responseReceivingStartDate = max(_responseReceivingStartDate, _requestSendingEndDate)
		// ensure response doesn't end before starting
		_executionEndDate = max(_executionEndDate, _responseReceivingStartDate)

		Response(out, err, _executionStartDate, _requestSendingEndDate, _responseReceivingStartDate, _executionEndDate)
	}
}
