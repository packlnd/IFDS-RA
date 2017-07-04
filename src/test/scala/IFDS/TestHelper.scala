package IFDS

class TestHelper {
  val foo = Method("foo")
  val bar = Method("bar")
  val a = Node("a")
  val b = Node("b")
  val b1 = Node("b1")
  val b2 = Node("b2")
  val c = Node("c")
  val c0 = Node("c0")
  val d = Node("d")
  val e = Node("e")
  val f = Node("f")
  val g = Node("g")
  val h = Node("h")
  val i = Node("i")
  val unused = Node("unused")
  val x = Node("x")
  val y = Node("y")
  val z = Node("z")

  def flow(source: String, targets: String*) = FlowFunc(1, source, targets: _*)
  def flow(times: Int, source: String, targets: String*) = FlowFunc(times, source, targets: _*)

  def mergeFlowAfterReturnSite: SimpleIFDSProblem = {
    val calAX = CallEdge(a, bar, Set(flow("0", "x")))
    val ctrAB = CallToReturnEdge(a, b, Set(flow("0", "b")))
    val norXY = NormalEdge(x, y, Set(flow("x", "y")))
    val retYB = ReturnEdge(Some(a), y, Some(b), bar, Set(flow("y", "c")))
    val norBC = NormalEdge(b, c, Set(flow("b", "1"), flow("c", "1")))

    val normalEdges: Set[NormalEdge] = Set(norXY, norBC)
    val callEdges: Set[CallEdge] = Set(calAX)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrAB)
    val returnEdges: Set[ReturnEdge] = Set(retYB)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a),
      bar -> Set(x)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
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
      false
    )
  }

  def notAllFlowsUsed: SimpleIFDSProblem = {

    // This demonstrates the flow
    val norAB = NormalEdge(a, b, Set(flow("0", "x")))
    val norBC = NormalEdge(b, c, Set(flow("x", "x")))
    val norDE = NormalEdge(d, e, Set(flow("y", "y", "z")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB, norAB, norBC, norDE)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      bar -> Set(a),
      foo -> Set(d)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> bar,
      b -> bar,
      c -> bar,
      d -> foo,
      e -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      Set(),
      Set(),
      Set(),
      stmtToMethod,
      Set(a),
      false
    )
  }

  def unexpectedFact: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.happyPath from Heros.
    // but changed fact
    val norAB = NormalEdge(a, b, Set(flow("0", "x")))
    val norBC = NormalEdge(b, c, Set(flow("unexpected fact", "x")))
    val calCFoo = CallEdge(c, foo, Set(flow("x", "y")))
    val ctrCF = CallToReturnEdge(c, f, Set(flow("x", "x")))
    val norDE = NormalEdge(d, e, Set(flow("y", "y", "z")))
    val retEF = ReturnEdge(Some(c), e, Some(f), foo, Set(flow("z", "u"), flow("y")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB, norBC, norDE)
    val callEdges: Set[CallEdge] = Set(calCFoo)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrCF)
    val returnEdges: Set[ReturnEdge] = Set(retEF)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      bar -> Set(a),
      foo -> Set(d)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> bar,
      b -> bar,
      c -> bar,
      f -> bar,
      d -> foo,
      e -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      false
    )
  }

  def artificalReturnEdgeForNoCallersCase: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.artificalReturnEdgeForNoCallersCase from Heros.
    val norAB = NormalEdge(a, b, Set(flow("0", "1")))
    val retBNull = ReturnEdge(None, b, None, foo, Set(flow("1", "1")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB)
    val returnEdges: Set[ReturnEdge] = Set(retBNull)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      Set(),
      Set(),
      stmtToMethod,
      Set(a),
      true
    )
  }

  def unbalancedReturn: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.unbalancedReturns from Heros.
    val norAB = NormalEdge(a, b, Set(flow("0", "1")))
    val retBY = ReturnEdge(Some(x), b, Some(y), foo, Set(flow("1", "1")))
    val norYZ = NormalEdge(y, z, Set(flow("1", "2")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB, norYZ)
    val returnEdges: Set[ReturnEdge] = Set(retBY)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      bar -> Set(unused),
      foo -> Set(a)
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
      Set(),
      Set(),
      stmtToMethod,
      Set(a),
      true
    )
  }

  def branch: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.branch from Heros.
    val norAB2 = NormalEdge(a, b2, Set(flow("0", "x")))
    val norAB1 = NormalEdge(a, b1, Set(flow("0", "x")))
    val norB1C = NormalEdge(b1, c, Set(flow("x", "x", "y")))
    val norB2C = NormalEdge(b2, c, Set(flow("x", "x")))
    val norCD = NormalEdge(c, d, Set(flow("x", "z"), flow("y", "w")))
    val norDE = NormalEdge(d, e, Set(flow("z"), flow("w")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB2, norAB1, norB1C, norB2C, norCD, norDE)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b1 -> foo,
      b2 -> foo,
      c -> foo,
      d -> foo,
      e -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      Set(),
      Set(),
      Set(),
      stmtToMethod,
      Set(a),
      false
    )
  }

  def reuseSummaryForRecursiveCall: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.reuseSummaryForRecursiveCall from Heros.
    val calABar = CallEdge(a, bar,  Set(flow("0", "1")))
    val ctrAB = CallToReturnEdge(a, b, Set(flow("0")))
    val norBC = NormalEdge(b, c, Set(flow("2", "3")))
    val norGI = NormalEdge(g, i, Set(flow("1", "1")))
    val norGH = NormalEdge(g, h, Set(flow("1", "1")))
    val calIBar = CallEdge(i, bar, Set(flow("1", "1")))
    val ctrIH = CallToReturnEdge(i, h, Set(flow("1")))
    val retHB = ReturnEdge(Some(a), h, Some(b), bar, Set(flow("1"), flow("2", "2")))
    val retHH = ReturnEdge(Some(i), h, Some(h), bar, Set(flow("1", "2"), flow("2", "2")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norBC, norGI, norGH)
    val callEdges: Set[CallEdge] = Set(calABar, calIBar)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrAB, ctrIH)
    val returnEdges: Set[ReturnEdge] = Set(retHB, retHH)

    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a),
      bar -> Set(g)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      g -> bar,
      h -> bar,
      i -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      false
    )
  }

  def reuseSummary: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.reuseSummary from Heros.
    val calABar = CallEdge(a, bar, Set(flow("0", "x")))
    val ctrAB = CallToReturnEdge(a, b, Set(flow("0", "y")))
    val calBBar = CallEdge(b, bar, Set(flow("y", "x")))
    val ctrBC = CallToReturnEdge(b, c, Set(flow("y")))
    val norCC0 = NormalEdge(c, c0, Set(flow("w", "0")))
    val norDE = NormalEdge(d, e, Set(flow("x", "z")))
    val retEB = ReturnEdge(Some(a), e, Some(b), bar, Set(flow("z", "y")))
    val retEC = ReturnEdge(Some(b), e, Some(c), bar, Set(flow("z", "w")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norCC0, norDE)
    val callEdges: Set[CallEdge] = Set(calABar, calBBar)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrAB, ctrBC)
    val returnEdges: Set[ReturnEdge] = Set(retEB, retEC)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      foo -> Set(a),
      bar -> Set(d)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> foo,
      b -> foo,
      c -> foo,
      c0 -> foo,
      d -> bar,
      e -> bar
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      false
    )
  }

  def happyPath: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.happyPath from Heros.
    val norAB = NormalEdge(a, b, Set(flow("0", "x")))
    val norBC = NormalEdge(b, c, Set(flow("x", "x")))
    val calCFoo = CallEdge(c, foo, Set(flow("x", "y")))
    val ctrCF = CallToReturnEdge(c, f, Set(flow("x", "x")))
    val norDE = NormalEdge(d, e, Set(flow("y", "y", "z")))
    val retEF = ReturnEdge(Some(c), e, Some(f), foo, Set(flow("z", "u"), flow("y")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB, norBC, norDE)
    val callEdges: Set[CallEdge] = Set(calCFoo)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrCF)
    val returnEdges: Set[ReturnEdge] = Set(retEF)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      bar -> Set(a),
      foo -> Set(d)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> bar,
      b -> bar,
      c -> bar,
      f -> bar,
      d -> foo,
      e -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      false
    )
  }

  def possiblyUninitializedVariables: SimpleIFDSProblem = {
    // This demonstrates the flow
    // This test is copied from IFDSSolverTest.happyPath from Heros.
    val norAB = NormalEdge(a, b, Set(flow("0", "x"), flow("0", "y")))
    val norBC = NormalEdge(b, c, Set(flow("y", "y"), flow("x")))
    val calCFoo = CallEdge(c, foo, Set(flow("y", "y")))
    val ctrCF = CallToReturnEdge(c, f, Set(flow("x", "x")))
    val norDE = NormalEdge(d, e, Set(flow("x", "x"), flow("y", "y")))
    val retEF = ReturnEdge(Some(c), e, Some(f), foo, Set(flow("y", "y")))
    ////////////

    val normalEdges: Set[NormalEdge] = Set(norAB, norBC, norDE)
    val callEdges: Set[CallEdge] = Set(calCFoo)
    val callToReturnEdges: Set[CallToReturnEdge] = Set(ctrCF)
    val returnEdges: Set[ReturnEdge] = Set(retEF)
    val methodToStartPoints: Map[Method, Set[Node]] = Map(
      bar -> Set(a),
      foo -> Set(d)
    )
    val stmtToMethod: Map[Node, Method] = Map(
      a -> bar,
      b -> bar,
      c -> bar,
      f -> bar,
      d -> foo,
      e -> foo
    )
    new SimpleIFDSProblem(
      methodToStartPoints,
      normalEdges,
      returnEdges,
      callEdges,
      callToReturnEdges,
      stmtToMethod,
      Set(a),
      false
    )
  }
}
