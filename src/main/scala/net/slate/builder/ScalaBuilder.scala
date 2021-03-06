/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 20th September 2011
 */
package net.slate.builder

import java.io.File
import net.slate.ExecutionContext._
import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.ConsoleReporter

/**
 *
 * @author Aishwarya Singhal
 *
 */
object ScalaBuilder extends Builder {

  /**
   * Compiles Scala files.
   */
  def build: List[Message] = {

    val (src, destDir, classpath, enableAlacs) = settings(currentProjectName)
    var sourceFiles = List[String]()

    src.foreach { dir =>
      findAllFiles(dir).filter { isModified(_, dir, destDir) }.foreach { sourceFiles ::= _ }
    }

    var errors = List[Message]()

    if (!sourceFiles.isEmpty && !buildInProgress) {
      buildInProgress = true
      configuration
      
      val settings = new Settings
      settings.outdir.value = destDir
      settings.classpath.value = classpath
      settings.deprecation.value = true
      settings.unchecked.value = true

      if (enableAlacs) {
        settings.plugin.value = List(alacsJar)
        settings.require.value = List("alacs")
      }

      val reporter = new ConsoleReporter(settings) {
        override def printMessage(msg: String) {
          if (msg.indexOf(": error:") != -1 || msg.indexOf(": warning:") != -1) { errors ::= Message.parse(msg) } else { println(msg) }
        }
      }
      val compiler = new Global(settings, reporter) // compiles the actual code

      try new compiler.Run compile (sourceFiles)
      catch {
        case ex: Throwable =>
          ex.printStackTrace()
          val msg = if (ex.getMessage == null) "no error message provided" else ex.getMessage
          println("Compile failed because of an internal compiler error (" + msg + "); see the error output for details.")
      }

      reporter.printSummary()
    }
    buildInProgress = false

    errors
  }

  def run(project: String, className: String, programArgs: String = "") = {
    execute(project, "scala", className, false, programArgs.split(" "))
  }

  def runTests(project: String, className: String, programArgs: String = "") = {
    execute(project, "scala", "org.scalatest.tools.Runner", true, Array("-p", ".", "-o", "-s", className))
  }

  protected def supportedExtension: String = ".scala"
}