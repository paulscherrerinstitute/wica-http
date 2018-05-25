module ch.psi.wica
{
   // The list of dependencies in this file was obtained using IntelliJ's
   // JPMS code analysis features. This saves a lot of donkey work !

   // Brian Goetz concurrency annotations
   // requires jcip.annotations;

   // requires epics
   requires ca;

   // used mainly for Validating method input parameters
   requires org.apache.commons.lang3;

   // logging
   requires slf4j.api;

   // reactor
   requires reactor.core;
   requires reactor.test;

   // junit
   requires junit;


   // JSON serialisation/deserialisation
   //requires jackson.annotations;
   //requires com.fasterxml.jackson.databind;
   //requires com.fasterxml.jackson.core;

   // Currently Spring 5 is on the path towards modularisation but this
   // process is not yet complete. The names below were obtained from
   // the MANIFEST.MF's Automatic-Module-Name entry which defines the module names
   // that will eventually be declared when the spring 'module-info.java'
   // files are finally created. Thus, in this release Spring's modules
   // should be considered 'automatic modules'

   requires spring.boot.test.autoconfigure;
   requires spring.boot.test;
   requires spring.test;
   requires spring.beans;
   requires spring.web;


   // The Spring-Boot framework accesses this application through two
   // mechanisms: (a) direct execution across module boundaries and (b) reflection.
   // The list below is for direct access.
   //exports ch.psi.wica.controllers;
   //exports ch.psi.wica.epics;

   // This list is to explicitly enable reflection.
   // Probably these lists can be reduced in scope when the dependencies
   // are better understood.

}