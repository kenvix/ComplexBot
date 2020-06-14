//--------------------------------------------------
// Class GenerialGui
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.gui

import com.kenvix.moecraftbot.ng.BuildConfig
import java.io.PrintStream
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.UIManager

class GeneralGui {
    private val frame: JFrame = JFrame(BuildConfig.APPLICATION_NAME + " - Console Window")
    private val textArea: JTextArea = JTextArea()
    private val scrollPane = JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    val outputStream by lazy { JTextAreaOutputStream(textArea) }
    val printStream by lazy { PrintStream(outputStream) }

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        frame.setSize(1280, 768)
        frame.add(scrollPane)
        textArea.lineWrap = true
    }

    fun show() {
        frame.isVisible = true
    }
}