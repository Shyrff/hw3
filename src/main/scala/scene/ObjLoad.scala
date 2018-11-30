package scene

import java.io.BufferedReader
import java.io.FileReader
import org.joml.Vector3f
import scala.collection.JavaConverters._

import java.util


case class Faces(vertex: Vector3f, normal: Vector3f)

class Model() {
  var vertices = new util.ArrayList[Vector3f]
  var normals = new util.ArrayList[Vector3f]
  var faces = new util.ArrayList[Faces]
}

object ObjLoad extends App {

  def getModelBounds(model: Model): (Float, Float, Float, Float, Float, Float) = {
    val vertices = model.vertices.asScala.toArray
    val xVertices = vertices.map(_.x)
    val yVertices = vertices.map(_.y)
    val zVertices = vertices.map(_.z)
    (xVertices.max, xVertices.min, yVertices.max, yVertices.min, zVertices.max, zVertices.min)
  }

  def getNormalizedModel(model: Model): Model = {
    val (xMax, xMin, yMax, yMin, zMax, zMin) = getModelBounds(model)
    val minVector = new Vector3f(xMin, yMin, zMin)
    val minNormals = new Vector3f(xMin, yMin, zMin)
    val normModel = new Model()
    normModel.vertices = new util.ArrayList[Vector3f](model.vertices.asScala.map(_.sub(minVector)).asJava)
    normModel.normals = model.normals
    normModel.faces = model.faces
    normModel
  }

  def translateModel(model: Model, translateVec: Vector3f): Model = {
    val normModel = new Model()
    normModel.vertices = new util.ArrayList[Vector3f](model.vertices.asScala.map(_.sub(translateVec)).asJava)
    normModel.normals = model.normals
    normModel.faces = model.faces
    normModel
  }

  def loadModel(fileName: String): Model = {
    val reader = new BufferedReader(new FileReader(fileName))
    val model = new Model()
    Iterator.continually(reader.readLine).takeWhile(_ != null).foreach(parseLine(_, model))

    model
  }

  def parseLine(line: String, model: Model): Unit = {
    if (line.startsWith("v ")) {
      val x = line.split(" ")(1).toFloat * 5
      val y = line.split(" ")(2).toFloat * 5
      val z = line.split(" ")(3).toFloat * 5
      model.vertices.add(new Vector3f(x, y, z))
    }
    else if (line.startsWith("vn ")) {
      val x = line.split(" ")(1).toFloat * 5
      val y = line.split(" ")(2).toFloat * 5
      val z = line.split(" ")(3).toFloat * 5
      model.normals.add(new Vector3f(x, y, z))
    }
    else if (line.startsWith("f ")) {
      val vertexIndices = new Vector3f(line.split(" ")(1).split("/")(0).toFloat,
        line.split(" ")(2).split("/")(0).toFloat ,
        line.split(" ")(3).split("/")(0).toFloat)
      val normalIndices = new Vector3f(line.split(" ")(1).split("/")(2).toFloat,
        line.split(" ")(2).split("/")(2).toFloat ,
        line.split(" ")(3).split("/")(2).toFloat)
      model.faces.add(Faces(vertexIndices, normalIndices))
    }
  }

}
