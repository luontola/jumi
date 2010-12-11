package net.orfjackal.dimdwarf.server

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import org.hamcrest.Matchers._
import org.hamcrest.MatcherAssert.assertThat
import net.orfjackal.dimdwarf.testutils.Sandbox
import java.io.File
import org.apache.commons.io._
import net.orfjackal.dimdwarf.auth._
import com.google.inject._
import org.junit.Assert

@RunWith(classOf[Specsy])
class ApplicationLoadingSpec extends Spec {
  val applicationDir = createTempDir()
  val classesDir = createDir(applicationDir, ApplicationLoader.CLASSES_DIR)
  val libDir = createDir(applicationDir, ApplicationLoader.LIBRARIES_DIR)

  "When configured correctly" >> {
    writePropertiesFile(classesDir, ApplicationLoader.CONFIG_FILE, Map(
      ApplicationLoader.APP_NAME -> "MyApp",
      ApplicationLoader.APP_MODULE -> classOf[MyApp].getName))
    copyClassToDir(classOf[MyApp], classesDir)
    val loader = new ApplicationLoader(applicationDir)

    "Adds to classpath the /classes directory" >> {
      FileUtils.write(new File(classesDir, "file.txt"), "file content")

      val content = readContent("file.txt", loader.getClassLoader)

      assertThat(content, is("file content"))
    }
    "Adds to classpath all JARs in the /lib directory" // TODO
    "Reads the application name from configuration" >> {
      assertThat(loader.getApplicationName, is("MyApp"))
    }
    "Reads the application module from configuration" >> {
      assertThat(loader.getApplicationModule, is(classOf[MyApp].getName))
    }
    "Instantiates the application module" >> {
      assertThat(loader.newModuleInstance, is(notNullValue[Module]()))
    }
  }

  "Error: configuration file is missing" >> {
    assertGivesAnErrorMentioning("File not found", ApplicationLoader.CONFIG_FILE)
  }
  "Error: no application name declared" >> {
    writePropertiesFile(classesDir, ApplicationLoader.CONFIG_FILE, Map(
      ApplicationLoader.APP_MODULE -> classOf[MyApp].getName))

    assertGivesAnErrorMentioning("Property", "was not set", ApplicationLoader.APP_NAME, ApplicationLoader.CONFIG_FILE)
  }
  "Error: no application module declared" >> {
    writePropertiesFile(classesDir, ApplicationLoader.CONFIG_FILE, Map(
      ApplicationLoader.APP_NAME -> "MyApp"))

    assertGivesAnErrorMentioning("Property", "was not set", ApplicationLoader.APP_MODULE, ApplicationLoader.CONFIG_FILE)
  }

  private def assertGivesAnErrorMentioning(messages: String*) {
    try {
      new ApplicationLoader(applicationDir)
      Assert.fail("should have thrown an exception")
    } catch {
      case e: ConfigurationException =>
        for (message <- messages) {
          assertThat(e.getMessage, containsString(message))
        }
    }
  }

  private def createTempDir(): File = {
    val sandbox = new Sandbox(new File("target"))
    val dir = sandbox.createTempDir()
    defer {sandbox.deleteTempDir(dir)}
    dir
  }

  private def createDir(parent: File, name: String): File = {
    val dir = new File(parent, name)
    assert(dir.mkdir())
    dir
  }

  private def writePropertiesFile(dir: File, name: String, properties: Map[String, String]) {
    import scala.collection.JavaConversions._
    val file = new File(dir, name)
    val rows = properties map {case (key, value) => key + "=" + value}
    FileUtils.writeLines(file, rows)
  }

  private def copyClassToDir(clazz: Class[_], dir: File) {
    val path = clazz.getName.replace('.', '/') + ".class"
    FileUtils.copyInputStreamToFile(clazz.getResourceAsStream("/" + path), new File(dir, path))
  }

  private def readContent(path: String, classLoader: ClassLoader): String = {
    val in = classLoader.getResourceAsStream(path)
    defer {in.close()}
    IOUtils.toString(in)
  }
}

class MyApp extends AbstractModule {
  protected def configure() {
    bind(classOf[CredentialsChecker[_]]).to(classOf[DummyCredentialsChecker])
  }
}

class DummyCredentialsChecker extends CredentialsChecker[Credentials] {
  def isValid(credentials: Credentials) = false
}
