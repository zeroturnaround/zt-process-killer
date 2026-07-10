/**
 *    Copyright (C) 2012 ZeroTurnaround LLC <support@zeroturnaround.com>
 *    Copyright (C) 2026 Neeme Praks
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
import aQute.bnd.gradle.BundleTaskExtension

plugins {
  `java-library`
  alias(libs.plugins.bnd)
  alias(libs.plugins.maven.publish)
}

group = "org.zeroturnaround"
version = "1.12.0"
description = "A library for stopping external processes from Java."

repositories {
  mavenCentral()
}

java {
  // Java 8 is the floor: jna 5.13.0 is compiled for Java 8, so the source/target
  // 1.7 of the historical Maven build can no longer be honoured. The toolchain is
  // auto-provisioned via the foojay resolver in settings.gradle.kts when no JDK 8
  // is installed.
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

dependencies {
  // jna and slf4j-api leak into the public API, so they are `api`: consumers
  // compiling against the exported packages need them on their compile classpath.
  //   - jna: the public binding interfaces org.zeroturnaround.process.unix.LibC,
  //     org.zeroturnaround.process.win.{W32API, Kernel32, W32Errors} extend
  //     com.sun.jna types (Library, StdCallLibrary, PointerType, ...).
  //   - slf4j: AbstractProcess (the documented base class for extension) exposes a
  //     `protected final org.slf4j.Logger log` field.
  api(libs.jna)
  api(libs.slf4j.api)

  // zt-exec, commons-lang3 and commons-io are used only inside method bodies and
  // never appear in any exported signature, so they are `implementation`:
  // consumers get them transitively at runtime but not on their compile classpath.
  // This narrows the transitive compile scope versus the old POM, where the
  // default `compile` scope put all of them on consumers' compile classpath.
  // (commons-io is in fact referenced only from the test sources today, but it
  // was a published runtime dependency, so it stays on the runtime classpath to
  // preserve the artifact contract.)
  implementation(libs.zt.exec)
  implementation(libs.commons.lang3)
  implementation(libs.commons.io)

  testImplementation(libs.junit)
  // slf4j-simple is the slf4j binding for tests: no transitive dependencies and
  // released in lockstep with slf4j-api, so there is no separate version to track.
  testRuntimeOnly(libs.slf4j.simple)
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
  options.encoding = "UTF-8"
  // The Javadoc predates the strict doclint in JDK 8+; don't fail the build on it.
  (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.withType<Test>().configureEach {
  // The test sources are JUnit 4; Gradle picks JUnit 4 up automatically from the
  // test classpath, so no useJUnitPlatform() here.
  testLogging {
    events("passed", "skipped", "failed")
  }
}

tasks.jar {
  // Reproduce the OSGi bundle headers from the historical bnd-maven-plugin setup.
  // No JPMS module-info is generated (there was no moditect plugin in the Maven build).
  val bundle = extensions.getByType(BundleTaskExtension::class.java)
  bundle.setBnd(
    """
    Bundle-SymbolicName: org.zeroturnaround.zt-process-killer
    Export-Package: org.zeroturnaround.process.*
    Specification-Version: ${project.version}
    """.trimIndent()
  )
}

// Sign only when in-memory keys are provided (CI release). Local builds skip signing.
tasks.withType<Sign>().configureEach {
  enabled = project.findProperty("signingInMemoryKey") != null
}

mavenPublishing {
  // The release workflow invokes `publishAndReleaseToMavenCentral`, which both
  // uploads and releases; no need to also auto-release from the plain task.
  publishToMavenCentral()
  signAllPublications()

  coordinates(group.toString(), "zt-process-killer", version.toString())

  pom {
    name.set("ZT Process Killer")
    description.set(project.description)
    url.set("https://github.com/zeroturnaround/zt-process-killer")
    licenses {
      license {
        name.set("The Apache Software License, Version 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        comments.set("A business-friendly OSS license")
      }
    }
    developers {
      developer {
        id.set("nemecec")
        name.set("Neeme Praks")
        email.set("neeme@praks.net")
        url.set("https://github.com/nemecec")
      }
      developer {
        id.set("rein")
        name.set("Rein")
      }
      developer {
        id.set("toomasr")
        name.set("Toomas")
      }
    }
    scm {
      url.set("https://github.com/zeroturnaround/zt-process-killer")
      connection.set("scm:git:git://github.com/zeroturnaround/zt-process-killer.git")
      developerConnection.set("scm:git:ssh://git@github.com/zeroturnaround/zt-process-killer.git")
    }
  }
}
