package spectra.commands

import java.util.Random

import org.apache.commons.io.FilenameUtils

import spectra.helpers.CommandHelper
import spectra.helpers.Config
import spectra.helpers.Environment
import spectra.helpers.Globals
import spectra.helpers.LogRecorder

class RecordCommand implements ShellCommand {
  CliBuilder cli
  LogRecorder recorder
  Environment environment
  String recordId
  File scriptFile
  String scriptName
  String scriptDesc
  Boolean isRecording
  Boolean isRecordEnv

  RecordCommand(LogRecorder recorder, Environment environment) {
    this.recorder = recorder
    this.environment = environment
    init()

    cli = new CliBuilder(usage:':record, :r [options]')
    // TODO: make a better header
    cli.header = 'First usage starts recording, second saves'
    cli.e('save environment in script', longOpt: 'environment')
    cli.f('file name and/or location', longOpt: 'file', args:1, argName:'name')
    cli.d('script description', longOpt: 'desc', args:1, argName:'description')
    cli.h('display this message', longOpt:'help')
  }

  String[] commandNames() { [':record', ':r'] }

  String run(args) {
    def message = commandOptions(args)
    if (message) return message

    if (!isRecording) {
      /* start recording */
      recordId = createScriptId()
      recorder.writeLine(startLine())
      isRecording = true
      return "Started recording script"
    } else {
      /* end recording */
      recorder.writeLine(endLine())
      def scriptLines = []
      def isScript = false
      recorder.getLogFile().eachLine { String line ->
        if (line == endLine()) return
        if (isScript && line.startsWith(Globals.PROMPT)) {
          line = line.substring(Globals.PROMPT.size())
          if (!line.startsWith(':')) scriptLines << line
        }
        if (line == startLine()) isScript = true
      }

      def scriptLoc = saveScript(scriptLines)
      init()
      return "Saved script to '$scriptLoc'"
    }
  }

  private String saveScript(scriptLines) {
    if (!scriptFile)
      scriptFile = new File(Config.getScriptDir(), "${recordId}.groovy")

    /* set environment vars */
    if (isRecordEnv) {
      def tmpEnv = "tmpEnvironmentFor_$recordId"
      def saveEnvLine = "$tmpEnv = environment"
      def envLineBuilder = new StringBuilder('environment = new Environment([')
      environment.getEnvironment().each{ key, val -> 
        envLineBuilder.append("'$key':'$val',")
      }
      envLineBuilder.setCharAt(envLineBuilder.length() - 1, ']' as char)
      envLineBuilder.append(')\n')

      scriptLines.add(0, envLineBuilder.toString())
      scriptLines.add(0, saveEnvLine)
      scriptLines << "\nenvironment = $tmpEnv"
    }

    /* create title comment */
    scriptName = scriptFile.getName()
    if (scriptName.contains('.')) scriptName = scriptName.split('\\.')[0]
    scriptDesc = scriptDesc ?: 'Spectra BlackPearl DSL script'

    def lineLength = 30
    def lineComment = { c -> "/*" + c * (lineLength + 2) + "*/\n" }
    def comment = (
      lineComment('*') +
      padComment(lineLength, scriptName) +
      lineComment('-') +
      padComment(lineLength, scriptDesc) +
      lineComment(' ') +
      padComment(lineLength, "Created on ${new Date().format('MM-dd-yyyy')}") +
      lineComment('*')
    )
    scriptLines.add(0, comment)
    scriptFile.write(scriptLines.join('\n'))

    scriptFile.toString()
  }

  /** Pads/divides a comment to keep the comment body uniform length */
  private String padComment(Integer lineLength, String text) {
    if (text.size() > lineLength) {
      // split line up into shorter lines while not splitting words
      def textLines = ['']
      def words = text.split(' ')
      words.each { String word ->
        if (textLines[-1].size() + 1 + word.size() > lineLength) {
          textLines << word
        } else {
          textLines[-1] = textLines[-1] + ' ' + word
        }
      }

      def commentLines = []
      textLines.each { String line ->
        commentLines << padComment(lineLength, line.trim())
      }
      return commentLines.join()
    } else {
      def padding = lineLength - text.size()
      return "/* " + text + (' ' * padding) + " */\n"
    }
  }

  /** @return help message if requested or error message */
  private String commandOptions(args) {
    def stringWriter = new StringWriter()
    cli.writer = new PrintWriter(stringWriter)
    def options = cli.parse(args)
    if (stringWriter.toString()) return stringWriter.toString()

    if (!options) return ''
    if (options.h) {
      cli.usage()
      return stringWriter.toString()
    }
    if (options.f) {
      def file = new CommandHelper().getScriptFromString(options.f)

      if (file.exists()) {
        return "[Error] The file $file already exists!\n"
      } else if (!file.getParentFile().exists()) {
        return "[Error] The directory ${file.getParent()} does not exist!\n"
      } else if (!(FilenameUtils.getExtension(file.toString()) in ['','groovy'])) {
        return "[Error] The script extension must be 'groovy' or none."
      } else {
        if (FilenameUtils.getExtension(file.toString()) == '')
          file = new File(file.toString() + '.groovy')
        scriptFile = file
      }
    }
    if (options.d) {
      this.scriptDesc = options.d
    }
    if (options.e) {
      this.isRecordEnv = true
    }
    return ''
  }

  /** sets fields to their default values */
  private void init() {
    this.recordId     = ''
    this.scriptDesc   = ''
    this.scriptName   = ''
    this.scriptFile   = null
    this.isRecording  = false
    this.isRecordEnv  = false

    File scriptDir = new File(Config.getScriptDir())
    if (!scriptDir.exists()) scriptDir.mkdirs()
  }

  private startLine() { "[START] $recordId" }
  private endLine() { "[END] $recordId" }

  /** Creates unique name for a recording session  */
  private createScriptId() {
    'script_' + (new Random().nextInt(10 ** 6))
  }
}