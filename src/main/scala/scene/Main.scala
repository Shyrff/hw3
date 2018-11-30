package scene

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) println("args: mode file. modes: 1 for view mode, 2 for 2 same obj on same scene")
    else{
      if (args(0) == "1") new ObjectViewer(args(1)).run()
      if (args(0) == "2") new DualObj(args(1)).run()
    }
  }
}
