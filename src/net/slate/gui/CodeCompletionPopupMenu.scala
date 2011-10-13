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
 *  Created on: 7th October 2011
 */
package net.slate.gui

import scala.swing.{ Action, Component }
import net.slate.Launch._

/**
 *
 * @author Aishwarya Singhal
 */
object CodeCompletionPopupMenu {
  import java.awt.event.KeyEvent
  import javax.swing.{ BorderFactory, JList, JScrollPane, KeyStroke, Popup, PopupFactory }
  import net.slate.editor.tools.CodeAssist

  var popup: Popup = null

  def show(owner: Component, x: Int, y: Int, list: Array[AnyRef]) {
    if (!list.isEmpty) {
      val factory = PopupFactory.getSharedInstance()
      val word = CodeAssist.getWord
      val contents = new JList(list)
      val scrollpane = new JScrollPane(contents)
      scrollpane.setBackground(java.awt.Color.decode("0xffffff"))
      scrollpane.setViewportBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5))

      if (popup != null) popup.hide

      popup = factory.getPopup(owner.peer, scrollpane, 210 + x, 110 + y)
      contents.addMouseListener(new java.awt.event.MouseAdapter {
        override def mouseClicked(e: java.awt.event.MouseEvent) {
          if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            val index = contents.locationToIndex(e.getPoint());
            val pane = currentScript.text
            list(index) match {
              case text: String =>
                pane.doc.remove(word._1, pane.caret.position - word._1)
                pane.doc.insertString(pane.caret.position, text, null)
            }
            restoreFocus
          }
        }

      })

      contents.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeCodeCompletion");
      contents.getActionMap().put("closeCodeCompletion", new javax.swing.AbstractAction {
        def actionPerformed(e: java.awt.event.ActionEvent) {
          restoreFocus
        }
      });
      popup.show()
    } else {
      if (popup != null) popup.hide
    }
  }

  def restoreFocus = {
    javax.swing.SwingUtilities.invokeLater(new Runnable {
      def run = {
        popup.hide
        currentScript.text.peer.requestFocus
      }
    })
  }
}