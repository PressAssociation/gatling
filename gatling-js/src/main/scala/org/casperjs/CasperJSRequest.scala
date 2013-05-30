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
package org.casperjs

import scala.sys.process._
import io.gatling.js.request.builder.CasperJSAttributes

/**
 * @author Bob Browning <bob.browning@pressassociation>
 */
case class CasperJSRequest(attributes: CasperJSAttributes) {

	val CASPERJS_EXECUTABLE = Option(System.getenv("casperjs.executable")).getOrElse("/usr/local/bin/casperjs")
	val PHANTOMJS_EXECUTABLE = Option(System.getenv("phantomjs.executable")).getOrElse("/usr/local/bin/phantomjs")

	val CasperEnvironment: Seq[Pair[String, String]] = Seq(
		"CASPERJS_EXECUTABLE" -> CASPERJS_EXECUTABLE,
		"PHANTOMJS_EXECUTABLE" -> PHANTOMJS_EXECUTABLE)

	def execute() =
		Process(
			CASPERJS_EXECUTABLE :: attributes.pathToFile :: attributes.arguments, None, CasperEnvironment: _*).!

	def execute(p: ProcessLogger) =
		Process(
			CASPERJS_EXECUTABLE :: attributes.pathToFile :: attributes.arguments, None, CasperEnvironment: _*).!(p)

	def isRunnable: Boolean = try {
		execute(ProcessLogger((log: String) => {})) == 0
	} catch {
		case _: Throwable => false
	}

}