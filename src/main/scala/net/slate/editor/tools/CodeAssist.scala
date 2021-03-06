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
package net.slate.editor.tools

import net.slate.Launch._

/**
 * Utility class that provides coding assistance.
 *
 * @author Aishwarya Singhal
 */
object CodeAssist {

  /**
   * Extract the word at the caret position.
   */
  def getWord: (Int, String) = getWord(true)

  def getWord(ignorePeriods: Boolean = true): (Int, String) = {
    val caret = currentScript.text.peer.getCaretPosition
    val doc = currentScript.text.peer.getDocument().asInstanceOf[javax.swing.text.DefaultStyledDocument]

    val start = doc.getParagraphElement(caret).getStartOffset
    val end = doc.getParagraphElement(caret).getEndOffset
    
    val caretPos = caret - start 
    
    val line = doc.getText(start, end - start)
    
    def min(a: Int, b: Int) = (a <= b || b == -1)
    def max(a: Int, b: Int) = (a >= b || b == -1)
    
    def index(x: Int, y: java.lang.Character, z: Int)(f: (Int, Int) => Boolean) = {
      if (line.contains(y) && f(z, x)) z else x
    } 
    
    val chars = Array(' ', '.', '(', '{', '[', ']', ')', '}')
    val endIndex = chars.foldLeft(-1){ (x, y) => index(x, y, line.indexOf(y, caretPos))(min) }
    val endPos = if (endIndex > -1) endIndex else line.length
    val startIndex = chars.foldLeft(-1){ (x, y) => index(x, y, line.lastIndexOf(y, caretPos))(max) }
    val startPos = if (startIndex > -1) startIndex + 1 else 0
    
    val word = if (startIndex == endIndex) "" else line.substring(startPos, endPos)
    val pos = caret - word.length

    (pos, word)
  }
}