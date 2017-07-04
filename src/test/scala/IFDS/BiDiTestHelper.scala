package IFDS

class BiDiTestHelper {
  val a = Node("a")
  val a1 = Node("a1")
  val a2 = Node("a2")
  val b = Node("b")
  val c = Node("c")
  val cs = Node("cs")
  val d = Node("d")
  val e = Node("e")
  val f = Node("f")
  val g = Node("g")
  val h = Node("h")
  val i = Node("i")
  val x = Node("x")
  val y = Node("y")
  val y1 = Node("y1")
  val y2 = Node("y2")
  val z = Node("z")

  val foo = Method("foo")
  val bar = Method("bar")

  def flow(source: String, targets: String*) = FlowFunc(1, source, targets: _*)
  def flow(times: Int, source: String, targets: String*) = FlowFunc(times, source, targets: _*)

  def happyPathBW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest.happyPath from Heros.
    val norCB = NormalEdge(c, b, Set())
    val norBA = NormalEdge(b, a, Set(flow("0", "2")))
    val retANull = ReturnEdge(None, a, None, foo, Set(flow("2")))
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norCB, norBA)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retANull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(c)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }
  def happyPathFW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest.happyPath from Heros.
    val norAB = NormalEdge(a, b, Set())
    val norBC = NormalEdge(b, c, Set(flow("0", "1")))
    val retCNull = ReturnEdge(None, c, None, foo, Set(flow("1")))
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAB, norBC)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retCNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }

  def unbalancedReturnsBothDirectionsFW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norAB = NormalEdge(a, b, Set())
    val norBC = NormalEdge(b, c, Set(flow("0", "1")))
    val retCZ = ReturnEdge(Some(y), c, Some(z), foo, Set(flow("1", "2")))

    val retZNull = ReturnEdge(None, z, None, bar, Set(flow("2")))
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAB, norBC)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retCZ, retZNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      z -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }
  def unbalancedReturnsBothDirectionsBW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norCB = NormalEdge(c, b, Set())
    val norBA = NormalEdge(b, a, Set(flow("0", "2")))
    val retAX = ReturnEdge(Some(y), a, Some(x), foo, Set(flow("2", "3")))

    val retXNull = ReturnEdge(None, x, None, bar, Set(flow("3")))
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norCB, norBA)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retAX, retXNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(c)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      x -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }

  def unbalancedReturnsNonMatchingCallSitesFW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norAB = NormalEdge(a, b, Set())
    val norBC = NormalEdge(b, c, Set(flow("0", "1")))
    val retCZ = ReturnEdge(Some(y1), c, Some(z), foo, Set(flow("1", "2")))

    val retZNull = ReturnEdge(None, z, None, bar, Set())
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAB, norBC)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retCZ, retZNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      z -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }

  def unbalancedReturnsNonMatchingCallSitesBW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norCB = NormalEdge(c, b, Set())
    val norBA = NormalEdge(b, a, Set(flow("0", "2")))
    val retAX = ReturnEdge(Some(y2), a, Some(x), foo, Set(flow("2", "3")))

    val retXNull = ReturnEdge(None, x, None, bar, Set())
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norCB, norBA)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retAX, retXNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(c)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      x -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }

  def returnsOnlyOneDirectionAndStopsFW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norAB = NormalEdge(a, b, Set())
    val norBC = NormalEdge(b, c, Set(flow("0", "1")))
    val retCZ = ReturnEdge(Some(y), c, Some(z), foo, Set(flow("1", "2")))

    val retZNull = ReturnEdge(None, z, None, bar, Set())
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAB, norBC)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retCZ, retZNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      z -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }

  def returnsOnlyOneDirectionAndStopsBW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norCB = NormalEdge(c, b, Set())
    val norBA = NormalEdge(b, a, Set(flow("0")))
    val retAX = ReturnEdge(Some(y), a, Some(x), foo, Set())

    val retXNull = ReturnEdge(None, x, None, bar, Set())
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norCB, norBA)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retAX, retXNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(c)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      x -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(b),
      true
    )
  }

  def reuseSummaryFW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norAB = NormalEdge(a, b, Set(flow("0", "1")))
    val calBBar = CallEdge(b, bar, Set(flow("1", "2")))
    val ctrBC = CallToReturnEdge(b, c, Set(flow("1")))
    val calCBar = CallEdge(c, bar, Set(flow("1", "2")))
    val ctrCD = CallToReturnEdge(c, d, Set(flow("1")))
    val retD = ReturnEdge(None, d, None, foo, Set(flow("1")))

    val norXY = NormalEdge(x, y, Set(flow("2", "2")))
    val retYC = ReturnEdge(Some(b), y, Some(c), bar, Set(flow("2", "1")))
    val retYD = ReturnEdge(Some(c), y, Some(d), bar, Set(flow("2", "1")))
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAB, norXY)
    val callEdges: Set[CallEdge] = Set(calBBar, calCBar)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrBC, ctrCD)
    val returnEdges: Set[ReturnEdge] = Set(retD, retYC, retYD)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      bar -> Set(x)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      d -> foo,
      x -> bar,
      y -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      true
    )
  }

  def reuseSummaryBW: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val retANull = ReturnEdge(None, a, None, foo, Set(flow("0")))
    ////////////
    val normalEdges: Set[NormalEdge] = Set()
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retANull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      true
    )
  }

  def dontResumeIfReturnFlowIsKilledFW: SimpleIFDSProblem = {
// forwardHelper.method("foo",
//   startPoints(), 
//   normalStmt("a", flow("0", "1")).succ("b"),
//   exitStmt("b").returns(over("cs"), to("y"), kill("1")));
// 
// forwardHelper.method("bar",
//   startPoints(),
//   normalStmt("y").succ("z" /* none */));
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norAB = NormalEdge(a, b, Set(flow("0", "1")))
    val retBY = ReturnEdge(Some(cs), b, Some(y), foo, Set(flow("1")))

    val norYZ = NormalEdge(y, z, Set())
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAB, norYZ)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retBY)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      y -> bar,
      z -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      true
    )
  }

  def dontResumeIfReturnFlowIsKilledBW: SimpleIFDSProblem = {
// backwardHelper.method("foo",
//   startPoints(),
//   normalStmt("a", flow("0", "1")).succ("c"),
//   exitStmt("c").returns(over("cs"), to("x"), flow("1", "2")));
// 
// backwardHelper.method("bar",
//   startPoints(),
//   normalStmt("x").succ("z" /*none*/));
    // This demonstrates the flow
    // This test is copied from BiDiIFDSSolverTest from Heros.
    val norAC = NormalEdge(a, c, Set(flow("0", "1")))
    val retCX = ReturnEdge(Some(cs), c, Some(x), foo, Set(flow("1", "2")))

    val norXZ = NormalEdge(x, z, Set())
    ////////////
    val normalEdges: Set[NormalEdge] = Set(norAC, norXZ)
    val callEdges: Set[CallEdge] = Set()
    val callToReturnEdges: Set[CallToReturnEdge] = Set()
    val returnEdges: Set[ReturnEdge] = Set(retCX)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      c -> foo,
      x -> bar,
      z -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      true
    )
  }

  // def multipleSeedsFW: SimpleIFDSProblem = {
  //   // This demonstrates the flow
  //   // This test is copied from BiDiIFDSSolverTest from Heros.
  //   val norA1B = NormalEdge(a1, b, Set(flow("0", "1")))
  //   val norA2B = NormalEdge(a2, b, Set(flow("0", "1")))
  //   val calBBar = CallEdge(b, bar, Set(flow(2, "1", "2")))
  //   val ctrBC = CallToReturnEdge(b, c, Set(flow(2, "1")))
  //   val calCBar = CallEdge(c, bar, Set(flow(2, "1", "2")))
  //   val ctrCD = CallToReturnEdge(c, d, Set(flow(2, "1")))
  //   val retDNull = ReturnEdge(None, d, None, foo, Set(flow(2, "1")))

  //   val norXY = NormalEdge(x, y, Set(flow("2", "2")))
  //   val retYC = ReturnEdge(Some(b), y, Some(c), bar, Set(flow(2, "2", "1")))
  //   val retYD = ReturnEdge(Some(c), y, Some(d), bar, Set(flow(2, "2", "1")))
  //   ////////////
  //   val normalEdges: Set[NormalEdge] = Set(norA1B, norA2B, norXY)
  //   val callEdges: Set[CallEdge] = Set(calBBar, calCBar)
  //   val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrBC, ctrCD)
  //   val returnEdges: Set[ReturnEdge] = Set(retDNull, retYC, retYD)
  //   val methodToStartPoints: Map[Method, Set[Node]] = Map(
  //     bar -> Set(x)
  //   )
  //   val stmtToMethod: Map[Node, Method] = Map(
  //     a1 -> foo,
  //     a2 -> foo,
  //     b -> foo,
  //     c -> foo,
  //     d -> foo,
  //     x -> bar,
  //     y -> bar
  //   )
  //   new SimpleIFDSProblem(
  //     methodToStartPoints,
  //     normalEdges,
  //     returnEdges,
  //     callEdges,
  //     callToReturnEdges,
  //     stmtToMethod,
  //     Set(a1, a2),
  //     true
  //   )
  // }

  // def multipleSeedsBW: SimpleIFDSProblem = {
  //   // This demonstrates the flow
  //   // This test is copied from BiDiIFDSSolverTest from Heros.
  //   val retA1Null = ReturnEdge(None, a1, None, foo, Set(flow("0")))
  //   val retA2Null = ReturnEdge(None, a2, None, foo, Set(flow("0")))
  //   ////////////
  //   val normalEdges: Set[NormalEdge] = Set()
  //   val callEdges: Set[CallEdge] = Set()
  //   val callToReturnEdges: Set[CallToReturnEdge] = Set()
  //   val returnEdges: Set[ReturnEdge] = Set(retA1Null, retA2Null)
  //   val methodToStartPoints: Map[Method, Set[Node]] = Map(
  //   )
  //   val stmtToMethod: Map[Node, Method] = Map(
  //     a1 -> foo,
  //     a2 -> foo
  //   )
  //   new SimpleIFDSProblem(
  //     methodToStartPoints,
  //     normalEdges,
  //     returnEdges,
  //     callEdges,
  //     callToReturnEdges,
  //     stmtToMethod,
  //     Set(a1, a2),
  //     true
  //   )
  // }

}
