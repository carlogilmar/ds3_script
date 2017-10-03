package com.spectralogic.dsl

import com.spectralogic.ds3client.networking.FailedRequestException
import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.dsl.helpers.DslCompleter
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import com.spectralogic.ds3client.utils.Guard
import jline.console.ConsoleReader
import jline.console.UserInterruptException
import org.apache.http.conn.ConnectTimeoutException
import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.LoggerFactory

/**
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 * TODO: change where log directory(s) is placed
 * TODO: test everything in README and use asserts instead of comments
 */
class Tool extends Script {
  private final static logger = LoggerFactory.getLogger(Tool.class)
  private final static recorder = new LogRecorder()

  static void main(String[] args) {
    InvokerHelper.runScript(Tool, args)
  }

  /** REPL handler */
  def run() {

    def shell = new ShellBuilder().build(this.class.classLoader)

    /* Run script passed in */
    if (args.size() > 0) {
//      TODO: make this better
      if (!args[0].endsWith('.groovy')) args[0] += '.groovy'
      def scriptArgs = args.size() > 1 ? args[1..-1] : []
      shell.run(new File(args[0]), scriptArgs)
      exit()
    }

    recorder.init()
    def console = new ConsoleReader()
    def commandFactory = new ShellCommandFactory(shell, recorder, console)

    console.setPrompt(Globals.PROMPT)
    console.setHandleUserInterrupt(true)
    console.addCompleter(new DslCompleter(shell))
    println Globals.initMessage(console.getTerminal().getWidth())

    try {
      while (true) {
        try {
          def line = console.readLine()
          def result = evaluate(shell, line, commandFactory)
          println Globals.RETURN_PROMPT + result
          recorder.record(line, result.toString())
        } catch (BpException | RuntimeException | FailedRequestException | ConnectTimeoutException e) {
          if (e in UserInterruptException) exit()
          logger.error('Exception: ', e)
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
    } finally {
      exit()
    }
  }

  /** Logic for parsing and evaluating a line */
  private static String evaluate(shell, line, commandFactory) {
    if (Guard.isStringNullOrEmpty(line)) return ''
    
    if (line.startsWith(':')) {
      /* command */
      def args = line.split(' ')
      def command = args[0]
      args = 1 < args.size() ? args[1..-1] : []
      def response = commandFactory.runCommand(command, args)
      response.log()
      
      if (response.exit) exit()
      return response.getMessage()
    } else {
      /* shell evaluation */
      return shell.evaluate(line)
    }
  }

  private static void exit() {
    recorder.close()
    System.exit(0)
  }

}
