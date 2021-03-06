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
package io.gatling.js.request.builder

import io.gatling.core.session._
import org.casperjs.{ CasperJSRequestFactory, CasperJSClient }
import io.gatling.js.action.CasperJSActionBuilder

case class CasperJSAttributes(requestName: Expression[String],
	pathToFile: String,
	arguments: List[Expression[String]] = List.empty,
	options: List[Pair[String, Expression[String]]] = List.empty)

/**
 * CasperJSRequestBuilder class companion
 */
object CasperJSRequestBuilder {
	def casperjs(requestName: Expression[String], pathToFile: String) =
		new CasperJSRequestBuilder(CasperJSAttributes(requestName, pathToFile))
}

/**
 * @author Bob Browning
 */
class CasperJSRequestBuilder(attributes: CasperJSAttributes) {

	require(CasperJSClient.default.isRunnable,
		"Could not run casperjs, try setting CASPERJS_EXECUTABLE and PHANTOMJS_EXECUTABLE environment properties.")

	def copy(attributes: CasperJSAttributes = this.attributes) = new CasperJSRequestBuilder(attributes)

	def arg(argument: Expression[String]) = copy(
		attributes.copy(arguments = argument :: attributes.arguments))

	def option(option: Pair[String, Expression[String]]) = copy(
		attributes.copy(options = option :: attributes.options))

	private[gatling] def build = CasperJSRequestFactory(attributes)

	private[gatling] def toActionBuilder = CasperJSActionBuilder(attributes.requestName, this.build)

}
