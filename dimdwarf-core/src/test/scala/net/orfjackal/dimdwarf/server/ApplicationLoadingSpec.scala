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

@RunWith(classOf[Specsy])
class ApplicationLoadingSpec extends Spec {
  val applicationDir = createTempDir()
  val classesDir = createDir(applicationDir, ApplicationLoader.APP_CLASSES)
  val libDir = createDir(applicationDir, ApplicationLoader.APP_LIBRARIES)

  writePropertiesFile(classesDir, ApplicationLoader.APP_PROPERTIES, Map(
    "dimdwarf.app.name" -> "MyApp",
    "dimdwarf.app.module" -> classOf[MyApp].getName))
  copyClassToDir(classOf[MyApp], classesDir)

  val loader = new ApplicationLoader(applicationDir)
  val classLoader = loader.getClassLoader

  "Adds to classpath the /classes directory" >> {
    FileUtils.write(new File(classesDir, "file.txt"), "file content")

    val content = readContent("file.txt", classLoader)

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

  "Error: configuration file is missing" // TODO
  "Error: no application name declared" // TODO
  "Error: no application module declared" // TODO


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
