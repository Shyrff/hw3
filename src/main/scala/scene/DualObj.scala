package scene

import camera.RotationCamera
import org.joml.{Matrix4f, Vector3f, Vector4f}
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW._
import org.lwjgl.glfw._
import org.lwjgl.opengl._
import org.lwjgl.system.MemoryUtil._
import org.lwjgl.opengl.GL11._

import scala.collection.JavaConverters._

class DualObj(modelPath: String) {
  val center = (1, 1)
  private val model = ObjLoad.translateModel(
    ObjLoad.getNormalizedModel(ObjLoad.loadModel(modelPath)),
    new Vector3f(center._1, 0f, center._2))
  private val model2 = ObjLoad.translateModel(
    ObjLoad.getNormalizedModel(ObjLoad.loadModel(modelPath)),
    new Vector3f(10f + center._1, 0f, 0 + center._2))

  val distanceToScreen = 15.0
  val screenHeight = 32.5
  val screenHeightPx = 1200

  var window = 0L
  var width = 800
  var height = 600
  var fbWidth = 800
  var fbHeight = 600
  var mouseX = .0f
  var mouseY = .0f
  var previousMouseX = .0f
  var previousMouseY = .0f
  var mouseYScroll = .0f
  var previousMouseYScroll = .0f
  var isMouseScroll = false
  var zoom = 1f

  val maxZoom = 20f
  val minZoom = 0.1f

  val keyDown = new Array[Boolean](GLFW.GLFW_KEY_LAST)
  val cam = new RotationCamera(maxZoom + 5)

  var mouseButtonPressed = false
  var isPreviousPressed = false
  val startPosition = new Vector3f(10f, 0f, 10f)
  val accFactor = 0.5f
  val scrollSensivity = 3f
  var rotateZ = 0.0f
  var isPerscpectiveMode = true

  val projMatrix = new Matrix4f
  val viewMatrix = new Matrix4f

  def run(): Unit = {
    try {
      init()
      loop()
      glfwDestroyWindow(window)
    } finally {
      glfwTerminate()
    }
  }

  def init(): Unit = {
    glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
    if (!glfwInit) throw new IllegalStateException("Unable to initialize GLFW")

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    window = glfwCreateWindow(width, height, "3D Object Viewer.", NULL, NULL)
    if (window == NULL) throw new RuntimeException("Failed to create the GLFW window")
    println("Press W/S to rotate up/down.")
    println("Press A/D to rotate left/right.")
    println("Press mouse and move it around for navigate.")
    println("Use mouse scroll to zoom +/-.")
    println("Press UP/DOWN to zoom +/-.")
    println("Press T for projection mode change")

    glfwSetKeyCallback(window, new GLFWKeyCallback() {
      override def invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true)
        if (action == GLFW_PRESS || action == GLFW_REPEAT) {
          keyDown(key) = true
        }
        else keyDown(key) = false
      }
    })
    glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
      override def invoke(window: Long, w: Int, h: Int): Unit = {
        if (w > 0 && h > 0) {
          fbWidth = w
          fbHeight = h
        }
      }
    })
    glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
      override def invoke(window: Long, w: Int, h: Int): Unit = {
        if (w > 0 && h > 0) {
          width = w
          height = h
        }
      }
    })
    glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
      override def invoke(window: Long, xpos: Double, ypos: Double): Unit = {
        mouseX = xpos.toInt - width / 2
        mouseY = height / 2 - ypos.toInt
      }
    })
    glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
      override def invoke(window: Long, button: Int, action: Int, mods: Int): Unit = {
        action match {
          case GLFW_RELEASE =>
            isPreviousPressed = false
            mouseButtonPressed = false
          case GLFW_PRESS =>
            mouseButtonPressed = true
          case _ =>
            throw new IllegalArgumentException("Unsupported mouse button action: " + action)
        }
      }
    })
    glfwSetScrollCallback(window, new GLFWScrollCallback() {
      override def invoke(window: Long, xoffset: Double, yoffset: Double): Unit = {
        previousMouseYScroll = mouseYScroll
        mouseYScroll = yoffset.toFloat
        isMouseScroll = (yoffset != 0)
      }
    })

    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor)
    glfwSetWindowPos(window, (vidmode.width - width) / 2, (vidmode.height - height) / 2)
    val framebufferSize = BufferUtils.createIntBuffer(2)
    nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4)
    fbWidth = framebufferSize.get(0)
    fbHeight = framebufferSize.get(1)
    glfwMakeContextCurrent(window)
    glfwSwapInterval(0)
    glfwShowWindow(window)
    glfwSetCursorPos(window, width / 2, height / 2)
  }

  def renderModel(model: Model, shadow: Boolean, isEdges: Boolean): Unit = {
    glBegin(GL_TRIANGLES)
    if (!isEdges) {
      if (shadow) glColor3f(0.2f, 0.2f, 0.2f)
      else glColor3f(0.0f, 0.0f, 0.2f)
      if (!shadow) {
        glColor3f(0.0f, 0.0f, 1.0f)
      }
    }
    if (shadow) glColor3f(0.2f, 0.2f, 0.2f)

    model.faces.asScala.foreach(face => {
      val n1 = model.normals.get(face.normal.x.toInt - 1)
      glNormal3f(n1.x, n1.y, n1.z)
      val v1 = model.vertices.get(face.vertex.x.toInt - 1)
      glVertex3f(v1.x, v1.y, v1.z)
      val n2 = model.normals.get(face.normal.y.toInt - 1)
      glNormal3f(n2.x, n2.y, n2.z )
      val v2 = model.vertices.get(face.vertex.y.toInt - 1)
      glVertex3f(v2.x, v2.y, v2.z)
      val n3 = model.normals.get(face.normal.z.toInt - 1)
      glNormal3f(n3.x, n3.y, n3.z )
      val v3 = model.vertices.get(face.vertex.z.toInt - 1)
      glVertex3f(v3.x, v3.y, v3.z)
    })
    glEnd()
  }

  def renderModelWithEdges(model: Model, shadow: Boolean): Unit = {
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    glColor3f(0.6f, 0.7f, 0.8f)
    renderModel(model, shadow, isEdges = false)
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    glEnable(GL_POLYGON_OFFSET_LINE)
    glPolygonOffset(-1.0f, -1.0f)
    glColor3f(0.2f, 0.2f, 0.2f)
    renderModel(model, shadow, isEdges = true)
    glDisable(GL_POLYGON_OFFSET_LINE)
  }

  def renderGrid(): Unit = {
    glBegin(GL_LINES)
    glColor3f(0.2f, 0.2f, 0.2f)
    val gridSize = 20 + center._1
    (-gridSize to gridSize).foreach{ i =>
      glVertex3f(-gridSize, 0.0f, i)
      glVertex3f(gridSize, 0.0f, i)
      glVertex3f(i, 0.0f, -gridSize)
      glVertex3f(i, 0.0f, gridSize)
    }
    glEnd()
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    glBegin(GL_QUADS)
    glColor3f(0.5f, 0.6f, 0.7f)
    glVertex3f(-gridSize, 0.0f, gridSize)
    glVertex3f(gridSize, 0.0f,  gridSize)
    glVertex3f(gridSize, 0.0f, -gridSize)
    glVertex3f(-gridSize, 0.0f, -gridSize)
    glEnd()
  }

  def renderLight(): Unit = {
    glPointSize(10.0f)
    glBegin(GL_POINTS)
    glColor3f(1.0f, 1.0f, 0.0f)
    glVertex3f(5.0f, 5.0f, 5.0f)
    glEnd()
  }


  def loop(): Unit = {
    GL.createCapabilities

    glClearColor(0.9f, 0.9f, 0.9f, 1.0f)
    glEnable(GL_LIGHT0)
    glLineWidth(1.4f)
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_STENCIL_TEST)
    glEnable(GL_CULL_FACE)
    glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)

    var lastTime = System.nanoTime
    val firstTime = lastTime
    val mat = new Matrix4f().translate(new Vector3f(center._1, 0f, center._2))

    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    glShadeModel(GL_SMOOTH)


    val fb = BufferUtils.createFloatBuffer(16)

    cam.setAlpha(Math.toRadians(40).toFloat)
    cam.setBeta(Math.toRadians(20).toFloat)
    cam.zoomMover.target = cam.zoomMover.current
    cam.zoomMover.velocity = 0
    zoom = cam.zoomMover.current.toFloat

    while (!glfwWindowShouldClose(window)) {
      val thisTime = System.nanoTime
      val diff = ((thisTime - lastTime) / 1E9).toFloat
      val angle = ((thisTime - firstTime) / 1E9).toFloat
      lastTime = thisTime
      renderGrid()

      processKeyboardClick()
      processMouse()

      cam.update(diff)
      glViewport(0, 0, width, height)
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT)

      if (isPerscpectiveMode) {
        mat.setPerspective(Math.atan((screenHeight * height / screenHeightPx) / distanceToScreen).toFloat,
          width.toFloat / height, 0.01f, 100.0f).lookAt(5.0f, 0.0f, 15.0f,
          0.0f, 0.0f, 0.0f,
          0.0f, 1.0f, 0.0f).get(fb)
      }
      else {
        mat.setOrtho(0f,  100, 0, 100, -100f, 100f).get(fb)

      }
      glEnable(GL_POLYGON_OFFSET_FILL)

      glMatrixMode(GL_PROJECTION)
      glLoadMatrixf(fb)


      cam.viewMatrix(mat.identity, isPerscpectiveMode).get(fb)
      glMatrixMode(GL_MODELVIEW)
      glLoadMatrixf(fb)

      glPolygonOffset(-1.0f, -1.0f)
      glEnable(GL_POLYGON_OFFSET_FILL)

      renderModelWithEdges(model, shadow = false)
      glDisable(GL_POLYGON_OFFSET_FILL)

      cam.viewMatrix(mat.identity, isPerscpectiveMode = false).get(fb)
      glMatrixMode(GL_MODELVIEW)
      glLoadMatrixf(fb)

      mat.setOrtho(-100f, 100f, -100f, 100f, -300f, 300f).lookAt(0.0f, 0.0f, 10.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f).get(fb)

      glMatrixMode(GL_PROJECTION)
      glLoadMatrixf(fb)

      glEnable(GL_POLYGON_OFFSET_FILL)

      renderModelWithEdges(model2, shadow = false)
      glDisable(GL_POLYGON_OFFSET_FILL)

      glfwSwapBuffers(window)
      glfwPollEvents()

    }
  }

  private def processKeyboardClick(): Unit = {
    val keyShift = 10f
    val zoomScaling = 1.11f
    if (keyDown(GLFW_KEY_D)) {
      cam.setAlpha(cam.getAlpha + Math.toRadians(-keyShift * 0.1f))
      cam.setBeta(cam.getBeta + Math.toRadians(0 * 0.1f))

    }
    if (keyDown(GLFW_KEY_A)) {
      cam.setAlpha(cam.getAlpha + Math.toRadians(keyShift * 0.1f))
      cam.setBeta(cam.getBeta + Math.toRadians(0 * 0.1f))

    }
    if (keyDown(GLFW_KEY_W)) {
      cam.setAlpha(cam.getAlpha + Math.toRadians(0 * 0.1f))
      cam.setBeta(cam.getBeta + Math.toRadians(keyShift * 0.1f))

    }
    if (keyDown(GLFW_KEY_S)) {
      cam.setAlpha(cam.getAlpha + Math.toRadians(0 * 0.1f))
      cam.setBeta(cam.getBeta + Math.toRadians(-keyShift * 0.1f))
    }
    if (keyDown(GLFW_KEY_T)) {
      isPerscpectiveMode = !isPerscpectiveMode
      cam.zoomMover.velocity = 0
      cam.zoomMover.target = cam.zoomMover.current
      zoom = maxZoom - cam.zoomMover.current.toFloat
      cam.zoom(zoom)
    }

    cam.zoomMover.maxAcceleration = 5f
    cam.zoomMover.maxDeceleration = 5f

    if (keyDown(GLFW_KEY_UP)) {
      if ((previousMouseYScroll * 1) <= 0) {
        cam.zoomMover.velocity = 0
        cam.zoomMover.target = cam.zoomMover.current
        zoom = cam.zoomMover.current.toFloat
        previousMouseYScroll = 1
      }
      else {
        zoom = zoom / zoomScaling
        zoom = if (zoom > maxZoom) maxZoom else (if (zoom < minZoom) minZoom else zoom)
        cam.zoom(zoom)
        previousMouseYScroll = 1
      }
    }
    if (keyDown(GLFW_KEY_DOWN)) {
      if ((previousMouseYScroll * (-1)) < 0) {
        cam.zoomMover.velocity = 0
        cam.zoomMover.target = cam.zoomMover.current
        zoom = cam.zoomMover.current.toFloat
        previousMouseYScroll = -1
      }
      else {
        zoom = zoom * zoomScaling
        zoom = if (zoom > maxZoom) maxZoom else (if (zoom < minZoom) minZoom else zoom)
        cam.zoom(zoom)
        previousMouseYScroll = -1
      }
    }
  }

  private def processMouse(): Unit = {
    if (mouseButtonPressed) {
      if (isPreviousPressed) {
        cam.setAlpha(cam.getAlpha + Math.toRadians((mouseX - previousMouseX) * 0.1f))
        cam.setBeta(cam.getBeta + Math.toRadians((previousMouseY - mouseY) * 0.1f))

        previousMouseX = mouseX
        previousMouseY = mouseY
      }
      else {
        isPreviousPressed = true
        previousMouseY = mouseY
        previousMouseX = mouseX
      }
    }

    val zoomScaling = 1.11f
    cam.zoomMover.maxAcceleration = 5f
    cam.zoomMover.maxDeceleration = 5f

    if (isMouseScroll) {
      if ((previousMouseYScroll * mouseYScroll) < 0) {
        cam.zoomMover.velocity = 0
        cam.zoomMover.target = cam.zoomMover.current
        zoom = cam.zoomMover.current.toFloat
      }
      else {
        zoom = if (mouseYScroll < 0) zoom * zoomScaling else zoom / zoomScaling
        zoom = if (zoom > maxZoom) maxZoom else (if (zoom < minZoom) minZoom else zoom)
        cam.zoom(zoom)
      }
      isMouseScroll = false
    }
  }
}