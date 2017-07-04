package flow.twist.trackable;

import soot.Unit;

public class Dummy extends Trackable {

	public static Dummy DUMMY = new Dummy();

	private Dummy() {

	}

	@Override
	public Trackable createAlias(Unit sourceUnits) {
		throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public Dummy cloneWithoutNeighborsAndPayload() {
		return DUMMY;
	}

  @Override
  public void setCallingContext(Trackable callingContext) {
  }

  @Override public boolean handleJoin(Trackable t) { return false; }
  @Override public JoinKey createJoinKey() { return null; }
}
