package com.spectralogic.dsl

import com.spectralogic.ds3client.commands.*
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.models.Bucket
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.ListBucketResult
import com.spectralogic.ds3client.models.User

import groovy.transform.*

import java.nio.file.Path
import java.nio.file.Paths

import com.spectralogic.dsl.helpers.Globals

/**
 * This is the customized SpectraShell that contains the DSL and acts similar
 * to GroovyShell
 */
abstract class SpectraDSL extends Script {

  /**
   * Points to a Global function since functions in an abstract class cannot be
   * be directly referenced and this function is used before init 
   * @return BpClient with given attributes or environment variables
   */
  def createBpClient(String endpoint="", String accessId="", 
                      String secretKey="", Boolean https=false) {
    Globals.createBpClient(endpoint, accessId, secretKey, https, environment)
  }

  /** Creates directory or file path from string  */
  def Path filePath(String dirName) {
    Paths.get(dirName)
  }

}