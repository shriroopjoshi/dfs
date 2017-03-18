package server;

public enum STATUS {

	SUCCESS("SUCCESS"),
	ERROR("ERROR");
	
	private final String msg;
	
	private STATUS(final String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return this.msg;
	}
}
