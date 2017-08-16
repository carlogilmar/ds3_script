package com.spectralogic.dsl

import com.spectralogic.dsl.helpers.Environment
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import com.spectralogic.dsl.models.BpClientFactory

class ShellBuilder {

  GroovyShell build(classLoader) {
    return new GroovyShell(classLoader, buildBinding(), buildConfig())
  }

  /** Builds shell configuration */
  private buildConfig() {
    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(buildImportCustomizer())
    config.scriptBaseClass = 'com.spectralogic.dsl.SpectraDSL'
    return config
  }

  /** Builds shell binding */
  private buildBinding() {
    def binding = new Binding()
    def environment = new Environment()
    if (environment.ready()) {
      binding.setVariable('client', new BpClientFactory().create())
    }
    binding.setVariable('environment', environment)
    return binding
  }

  /** Builds object to pass imports into the shell */
  private buildImportCustomizer() {
    def importCustomizer = new ImportCustomizer()
    importCustomizer.addImport('com.spectralogic.dsl.helpers.Environment')
    return importCustomizer
  }

}