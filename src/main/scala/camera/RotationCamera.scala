package camera

import org.joml.Matrix4f
import org.joml.camera.{ArcRotor, ScalarMover, Vector3Mover}

class RotationCamera(maxZoom: Float) {
  var centerMover: Vector3Mover = new Vector3Mover()
  centerMover.maxDirectAcceleration = 5.0f
  centerMover.maxDirectDeceleration = 5.0f

  val alphaMover = new ArcRotor
  val betaMover = new ArcRotor
  val zoomMover: ScalarMover = new ScalarMover()

  zoomMover.current = 10.0f
  zoomMover.target = 10.0f
  zoomMover.maxAcceleration = 10.0f
  zoomMover.maxDeceleration = 15.0f


  def viewMatrix(mat: Matrix4f, isPerscpectiveMode: Boolean = true): Matrix4f = {

    if (isPerscpectiveMode) {
      mat.translate(0, 0, -zoomMover.current.toFloat)
        .rotateX(betaMover.current.toFloat)
        .rotateY(alphaMover.current.toFloat)
        .translate(-centerMover.current.x, -centerMover.current.y, -centerMover.current.z)
    }
    else {
      mat.translate(0, 0, -zoomMover.current.toFloat)
        .scale(maxZoom - zoomMover.current.toFloat, maxZoom - zoomMover.current.toFloat, maxZoom - zoomMover.current.toFloat)
        .rotateX(betaMover.current.toFloat)
        .rotateY(alphaMover.current.toFloat)
        .translate(-centerMover.current.x, -centerMover.current.y, -centerMover.current.z)
    }

  }

  def setAlpha(alpha: Double): Unit = {
    alphaMover.target = alpha % (2.0 * Math.PI)
  }

  def setBeta(beta: Double): Unit = {
    betaMover.target = if (beta < -Math.PI / 2.0) -Math.PI / 2.0
    else if (beta > Math.PI / 2.0) Math.PI / 2.0
    else beta
  }

  def getAlpha: Double = alphaMover.target

  def getBeta: Double = betaMover.target

  def zoom(zoom: Double): Unit = {
    zoomMover.target = zoom
  }

  def center(x: Float, y: Float, z: Float): Unit = {
    centerMover.target.set(x, y, z)
  }

  def update(elapsedTimeInSeconds: Float): Unit = {
    alphaMover.update(elapsedTimeInSeconds)
    betaMover.update(elapsedTimeInSeconds)
    zoomMover.update(elapsedTimeInSeconds)
    centerMover.update(elapsedTimeInSeconds)
  }
}
