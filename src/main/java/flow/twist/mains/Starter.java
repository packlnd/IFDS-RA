package flow.twist.mains;

import java.util.*;

class Starter {
  public static void main(String[] args) {
    List<String> argList = new ArrayList<String>(Arrays.asList(args));
    System.out.println(Arrays.toString(args));
    String arg1 = argList.remove(0);
    String arg2 = argList.remove(0);
    String arg3 = argList.remove(0);
    args = new String[args.length-3];
    argList.toArray(args);
    System.out.println(Arrays.toString(args));
    if (arg1.equals("heros") && arg2.equals("soot")) {

      if (arg3.equals("fw")) ClassForNameForwardsFromStringParams.main(args);
      else if (arg3.equals("bidi")) ClassForNameBiDi.main(args);
      else if (arg3.equals("gen")) GenericCallerSensitiveBiDi.main(args);

    } else if (arg1.equals("ra") && arg2.equals("soot")) {

      if (arg3.equals("fw")) RA.main(args);
      else if (arg3.equals("bidi")) BiDiRA.main(args);
      else if (arg3.equals("gen")) GenBiDiRA.main(args);
      else if (arg3.equals("seq")) RASEQ.main(args);

    } else {
      throw new IllegalArgumentException();
    }
  }
}
